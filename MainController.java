package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {
	
	@FXML private Button imageEncryption;
	@FXML private Button LoadImage;
	@FXML Button button;
	@FXML Button button2;
	@FXML AnchorPane anchorPane;
	@FXML private Text passwordMatch;
	@FXML private PasswordField dataHidingKey;
	@FXML private ImageView embeddedView;
	@FXML private PasswordField dataHidingKeyConfirmation;
	@FXML private Text passwordConfirmation;
	@FXML private TextArea decodedMessageText;
	@FXML private Text messageTooLong;
	@FXML private PasswordField dataHiding;
	private @FXML ImageView imageView;
	private @FXML ImageView preprocessedView;
	private @FXML TextArea secretMessage;
	private String eKey = "";
	private String sMessage = "";
	private @FXML ImageView markedImage;
	private @FXML PasswordField encryptionKey;
	private @FXML ImageView encryptedView;
	private @FXML ImageView decryptedImage;
	private @FXML PasswordField decodePassword;
	private @FXML BufferedImage newBI;
	private @FXML PasswordField confirmKey;


	public void changeLoadUI(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/Main.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(scene);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeDataEmbeddingUI(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("dataEmbeddingUI.fxml"));
			Scene dataEmbeddingScene = new Scene(root);
			dataEmbeddingScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(dataEmbeddingScene);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeDataExtractionUI(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("dataExtractionUI.fxml"));
			Scene dataExtractionScene = new Scene(root);
			dataExtractionScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(dataExtractionScene);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void uploadImage(ActionEvent event) throws FileNotFoundException {
		
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("PNG Files", "*.PNG", "JPG Files", "*JPG", "*JPEG", "JPEG Files"));
		File selectedFile = fc.showOpenDialog(null);
		
		if(selectedFile != null) {
			System.out.println(selectedFile.getAbsolutePath());
			FileInputStream inputstream = new FileInputStream(selectedFile.getAbsolutePath()); 
			
			Image image = new Image(inputstream);
		
			imageView.setImage(image);
		}
		else {
			System.out.println("File is not valid");
		}
	}
	
	public void encrypt(ActionEvent event) { 
		
		Random r = new Random();
		eKey = encryptionKey.getText();

		if(eKey.compareTo(confirmKey.getText()) == 0) {
			passwordMatch.setVisible(false);
			
			long seed = eKey.hashCode();
			r.setSeed(seed);
		
			Image source = preprocessedView.getImage();
		
			int w = (int)source.getWidth();
			int h = (int)source.getHeight();
			BufferedImage encryptedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			for(int height = 0; height < h; height++) {
				for(int width = 0; width < w; width++) {
					
					int randomInt = r.nextInt();
										
					//gets binary sequence for encryption based on first 8 bits of the random number generated
					int binarySequence = randomInt & 0xff;
					
					int sourcePixel = source.getPixelReader().getArgb(width, height); 
					int redChannel = (sourcePixel>>16) & 0xff;	//red channel
					int alphaChannel = (sourcePixel>>24) & 0xff;
					int XOR = (binarySequence | redChannel) &  (~binarySequence | ~redChannel);
				
					redChannel = XOR;
						
					int encryptedBinary = (alphaChannel<<24) | (redChannel<<16) | (redChannel<<8) | redChannel;
				
					encryptedImage.setRGB(width, height, encryptedBinary);
				}
				
			}
			
			Image image = SwingFXUtils.toFXImage(encryptedImage, null); 
			saveImage(encryptedImage);
			encryptedView.setImage(image);
		}
		else {
			System.out.println("Passwords don't match");
			passwordMatch.setVisible(true);
		}	
		
	}
	
	private void saveImage(BufferedImage encryptedImage) {
		
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("PNG Files", "*.PNG", "JPG Files", "*JPG", "*JPEG", "JPEG Files"));
		
		File outputFile = fc.showSaveDialog(null);
		
		try {
			ImageIO.write(encryptedImage,"png", outputFile);
		}
		catch(Exception e) {
			System.out.println("");
		}
	}
	
	public void preprocessButton(ActionEvent event) {
		preprocess(imageView.getImage());
	}
	
	public void preprocess(Image source) {
		
		int w = (int)source.getWidth();
		int h = (int)source.getHeight();
		
		BufferedImage prediction = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	
		for(int height = 0; height < h; height++) {
			for(int  width = 0; width < w; width++) {				
				
				int pred = source.getPixelReader().getArgb(width, height); //copies argb values
				int copy = 0;
				int leftPixel = 0, upPixel = 0, redLeft = 0, redUp = 0;
				int redModified = 0;
				
				if(width != 0) {
					leftPixel = prediction.getRGB(width - 1, height); //gets Left Pixel Value
					redLeft = (leftPixel>>16) & 0xff; //red channel left pixel value
				}
				if(height != 0) {
					upPixel = prediction.getRGB(width, height - 1);
					redUp = (upPixel>>16) & 0xff; //red channel up pixel value
				}
				
				//get each value as an integer (0-256) for each pixel
				
				int a = (pred>>24) & 0xff;   //alpha channel
				int r = (pred>>16) & 0xff;	//red channel
				
				int rPred = 0;
					
				//pre-processing algorithm
				int inv = ((r + 128) % 256);  //inverse pixel
				
				if(height == 0 && width == 0) { //first pixel
					rPred = r;
				}
				else if(height == 0 && width != 0) { //first row
					rPred = redLeft;
				}
				else if(width == 0 && height != 0) { //first column
					rPred = redUp;
				}
				else if(width != 0 && height != 0){
					rPred = (redLeft + redUp) / 2;
				}
			
				if((Math.abs(rPred - r)) >= (Math.abs(rPred - inv))) { //Set MSB Value
					if(r < 128) {
						redModified = rPred - 63;
					}
					else {
						redModified = rPred + 63;
					}
				}
				else {
					redModified = r;
				}
			
				copy =  (a<<24) |(redModified<<16) | (redModified<<8) | redModified; //Copies argb values (int)
	
				prediction.setRGB(width, height, copy);	
			}	
			
		}
		//Changes Bufferred image into image
		Image image = SwingFXUtils.toFXImage(prediction, null); 
		preprocessedView.setImage(image);
		
	}
	
	//Method to convert a string with characters to a binary string
	private String encode(String message) {
		char[] characters = message.toCharArray();
	    String returnString = "";
	    String preProcessed = "";

	    for(int i = 0; i < characters.length; i++) {
	        //Converts the character to a binary string
	        preProcessed = Integer.toBinaryString((int)characters[i]);
	        //Adds enough zeros to the front of the string to make it a byte(length 8 bits)
	        String zerosToAdd = "";
	        if(preProcessed.length() < 8) {
	            for(int j = 0; j < (8 - preProcessed.length()); j++) {
	                zerosToAdd += "0";
	            }
	            
	        }
	        returnString += zerosToAdd + preProcessed;
	    }

	    //Returns a string with a length that is a multiple of 8
	    return returnString;
	    
	}
	
	//method to embed message into image and embed message length in binary
	public void embedMessage(ActionEvent event) {
	
		Image source = imageView.getImage();
		int w = (int)source.getWidth();
		int h = (int)source.getHeight();
		BufferedImage embeddedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		Random r = new Random();
		String dataHiding = dataHidingKey.getText();
		sMessage = secretMessage.getText();
		
		String binaryString = encode(sMessage);
		int binaryLength = binaryString.length();
		String binaryLengthString = Integer.toBinaryString(binaryLength);
		
		if(binaryLength >= w * h) {
			messageTooLong.setVisible(true);
			System.out.println("Message is too long");
		}
		
		if(dataHiding.compareTo(dataHidingKeyConfirmation.getText()) == 0 && (binaryLength < w * h)) {
			
			messageTooLong.setVisible(false);
			passwordConfirmation.setVisible(false);
			long seed = dataHiding.hashCode();
			r.setSeed(seed); 
						
			int counter1 = 0;
			for(int width = 0; width < 33; width++) {
				int color = source.getPixelReader().getArgb(width, 0);
				int redChannel = (color>>16) & 0xff;	
			}
			
			//To encrypt length of message in first 32 pixels
			for(int width = 0; width < (33 - binaryLengthString.length()); width++) {
			
				int color = source.getPixelReader().getArgb(width, 0);
				int redChannel = (color>>16) & 0xff;
				int a = (color>>24) & 0xff;
				if(width == 0) {
					int finalValue = (a<<24) | (redChannel<<16) | (redChannel<<8) | redChannel;
					embeddedImage.setRGB(0, 0, finalValue);
					continue;
				}
				if(redChannel >= 128) {
					redChannel = ((redChannel + 128) % 256);
				}
				int finalValue = (a<<24) | (redChannel<<16) | (redChannel<<8) | redChannel;
				embeddedImage.setRGB(width, 0, finalValue);
				counter1++;
			}
			
			System.out.println("counter 1 " + counter1);
			int counter2 = 0; 
			
			for(int width = counter1 + 1; width < 33; width++) {
				
				int color = source.getPixelReader().getArgb(width, 0);
				int redChannel = (color>>16) & 0xff;
				int a = (color>>24) & 0xff;
				int val = (int)binaryLengthString.charAt(counter2) - '0';
				
				int valueInserted = val * 128 + (redChannel % 128);
				int finalValue = (a<<24) | (valueInserted<<16) | (valueInserted<<8) | valueInserted;
			
				embeddedImage.setRGB(width, 0, finalValue);
				counter2++;
			}
			
			for(int width = 0; width < 33; width++) {
				int color = embeddedImage.getRGB(width, 0);
				int redChannel = (color>>16) & 0xff;
			}
			
			int counter = 0;
		
			for(int height = 0; height < h; height++) {
				for(int width = 0; width < w; width++) {
					if(counter != binaryString.length()) {
						//Skip first 33 pixels
						if(width < 33 && height == 0) {
							continue;
						}
				
						int color = source.getPixelReader().getArgb(width, height); //copies argb values
						int redChannel = (color>>16) & 0xff;
						int a = (color>>24) & 0xff;
									
						int val = (int)binaryString.charAt(counter) - '0';
						int randomInt = r.nextInt();
						
						//gets binary sequence for encryption based on first 8 bits of the random number generated
						int binarySequence = randomInt & 0xff;
						int msbOfBinarySequence; //use msb to encrypt each bit of the message
						
						if(binarySequence >= 128) {
							msbOfBinarySequence = 1;
						}
						else {
							msbOfBinarySequence = 0;
						}
						
						int XOR = (msbOfBinarySequence | val) &  (~msbOfBinarySequence | ~val);
						
						int xorValue = XOR * 128 + (redChannel % 128);
						int finalValue = (a<<24) | (xorValue<<16) | (xorValue<<8) | xorValue;
						counter++;
						embeddedImage.setRGB(width, height, finalValue);
		
					}
					else {
						int color = source.getPixelReader().getArgb(width, height);
						int redChannel = (color>>16) & 0xff;
						int a = (color>>24) & 0xff;
						int finalValue = (a<<24) | (redChannel<<16) | (redChannel<<8) | redChannel;
						embeddedImage.setRGB(width, height, finalValue);
					}	
				}
			}
			
			Image embed = SwingFXUtils.toFXImage(embeddedImage, null); 
			saveImage(embeddedImage);
			embeddedView.setImage(embed);
		}
		else {
			passwordConfirmation.setVisible(true);
		}
	}
	
	//method to decode the secret message
	public void decodeImage(ActionEvent event){
	
		Image source = imageView.getImage();
		Random r = new Random();
		String dataHidingK = dataHiding.getText();
		long seed = dataHidingK.hashCode();
		r.setSeed(seed); 
		
		int w = (int)source.getWidth();
		int h = (int)source.getHeight();
	
		String binaryStringLength = "";
		
		for(int width = 1; width < 33; width++) {
			int color = source.getPixelReader().getArgb(width, 0); //copies argb values
			int redChannel = (color>>16) & 0xff;
			if (redChannel >= 128) {
				binaryStringLength += "1";
			}
			else {
				binaryStringLength += "0";
			}
		}
		
		int decodedLength = Integer.parseInt(binaryStringLength, 2);
		String decodedMessage = "";
		int counter = 0;
		
		for(int height = 0; height < h; height++) {
			for(int width = 0; width < w; width++) {
				if(width < 33 && height == 0) {
					continue;
				}
				if(counter == decodedLength) {
					break;
				}
				int color = source.getPixelReader().getArgb(width, height); //copies argb values
				int redChannel = (color>>16) & 0xff;
				int randomNumber = r.nextInt();
				int binarySequence = randomNumber & 0xff;
				int msbOfBinarySequence;
				if(binarySequence >= 128) {
					msbOfBinarySequence = 1;
				}
				else 
					msbOfBinarySequence = 0;
				int msb;
				if(redChannel >= 128) {
					msb = 1;
				}
				else {
					msb = 0;
				}
	
				int XOR = (msbOfBinarySequence | msb) &  (~msbOfBinarySequence | ~msb);
				
				if(XOR == 1) {
					decodedMessage += "1";
				}
				else {
					decodedMessage += "0";
				}
				
				counter++;
			}
			if(counter == decodedLength) {
				break;
			}
		}
		
		String finalMessage = decrypt(decodedMessage);
		decodedMessageText.setText(finalMessage);
	}
	
	//method to decode the encrypted image
	public void decode(ActionEvent event) {
		
		Image source = imageView.getImage();
		String inputPassword = decodePassword.getText();
		int w = (int)source.getWidth();
		int h = (int)source.getHeight();
		
		Random r = new Random();
		int seed = inputPassword.hashCode();
		r.setSeed(seed);
		
		BufferedImage decodedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		BufferedImage finalImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		for(int height = 0; height < h; height++) {
			for(int width = 0; width < w; width++) {
				
				int randomInt = r.nextInt();
				int binarySequence = randomInt & 0xff;
				int sourcePixel = source.getPixelReader().getArgb(width, height); 
				int redChannel = (sourcePixel>>16) & 0xff;	//red channel
				int alphaChannel = (sourcePixel>>24) & 0xff;
				int XOR = (binarySequence | redChannel) &  (~binarySequence | ~redChannel);
				redChannel = XOR;
		
				int encryptedBinary = (alphaChannel<<24) | (redChannel<<16) | (redChannel<<8) | redChannel;
				decodedImage.setRGB(width, height, encryptedBinary);
				if(width == 0 && height == 0) {
					finalImage.setRGB(width, height, encryptedBinary);
					continue;
				}
				
				int leftPixel = 0, upPixel = 0, redLeft = 0, redUp = 0;
				int prediction = 0;
				int sourceRed = redChannel;
				
				if(width != 0) {
					leftPixel = finalImage.getRGB(width - 1, height); //gets Left Pixel Value
					redLeft = (leftPixel>>16) & 0xff; //red channel left pixel value
				}
				if(height != 0) {
					upPixel = finalImage.getRGB(width, height - 1); //gets Up Pixel Value
					redUp = (upPixel>>16) & 0xff; //red channel up pixel value
				}
				
				if(width != 0 && height != 0) {
					prediction = (redLeft + redUp) / 2;
				}
				else if(height == 0 && width != 0){
					prediction = redLeft;
				}
				else if(width == 0 && height != 0) {
					prediction = redUp;
				}
				
				int msbON = 0, msbOFF = 0;
				int inv = (sourceRed + 128)  % 256;
				
				if(sourceRed >= 128) {
					msbON = sourceRed;
					msbOFF = (sourceRed + 128)  % 256;
				}
				else{
					msbON = (sourceRed + 128)  % 256;
					msbOFF = sourceRed;
				}
				
				int redModified;
				
				if((Math.abs(msbOFF - prediction)) < (Math.abs(msbON - prediction))) {
					redModified = msbOFF;
				}
				else {
					redModified = msbON;
				}
				
				int finalBinary = (alphaChannel<<24) | (redModified<<16) | (redModified<<8) | redModified;
				finalImage.setRGB(width, height, finalBinary);
			}
		}
		
		Image finalIMG = SwingFXUtils.toFXImage(finalImage, null); 
		decryptedImage.setImage(finalIMG);
		
	}
	
	//method to change from binary string to character string
	public String decrypt(String message) {
	    //Check to make sure that the message is all 1s and 0s.
	    for(int i = 0; i < message.length(); i++) {
	        if(message.charAt(i) != '1' && message.charAt(i) != '0') {
	            return null;
	        }
	    }

	    //If the message does not have a length that is a multiple of 8, we can't decrypt it
	    if(message.length() % 8 != 0) {
	        return null;
	    }

	    //Splits the string into 8 bit segments with spaces in between
	    String returnString = "";
	    String decrypt = "";
	    for(int i = 0; i < message.length() - 7; i += 8) {
	        decrypt += message.substring(i, i + 8) + " ";
	    }

	    //Creates a string array with bytes that represent each of the characters in the message
	    String[] bytes = decrypt.split(" ");
	    for(int i = 0; i < bytes.length; i++) {
	        //Decrypts each character and adds it to the string to get the original message
	        returnString += (char)Integer.parseInt(bytes[i], 2);
	    }

	    return returnString;
	}
}
