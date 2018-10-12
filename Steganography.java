import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.Charset;

/**
*Steganography is a class that can hide and retrieve a hidden message in a PNG image.
*
*@author	Toby Flynn
*/
public class Steganography {
	
	public static void main(String[] args) {
		if(args[0].equals("e")) {
			//Encode a message in an image
			if(args.length != 4) {
				System.out.println("Please enter the mode ('e' or 'd'), input file, output file and hidden message as arguments");
				System.exit(1);
			}
			
			Steganography s = new Steganography();
			s.hideMessage(args[1], args[2], args[3]);
		} else if(args[0].equals("d")) {
			//Decode a message from an image
			if(args.length != 2) {
				System.out.println("Please enter the mode ('e' or 'd') and input file");
				System.exit(1);
			}
			
			Steganography s = new Steganography();
			System.out.println(s.getMessage(args[1]));
		} else {
			System.out.println("The first argument must either be 'e' (to encode a message in a png) or 'd' (to decode a message from a png)");
		}
	}
	
	/**
	*Method that will hide a message (String) in the last bit of the RGB values of each pixel in a PNG image.
	*
	*@param 		inputFile		Path to the input image
	*@param			outputFile		Path to where the new image (with the hidden message) will be written to
	*@param			message			The message that will be hidden in the image
	*/
	public void hideMessage(String inputFile, String outputFile, String message) {
		ImageHandler imgHandler = new ImageHandler(inputFile);
		//Load PNG as a BufferedImage
		BufferedImage img = imgHandler.getImage();
		//Insert the message into the BufferedImage
		try {
			img = insertMessage(img, message);
		} catch (MessageTooLargeException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ImageTooSmallException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//Write the BufferedImage to disk
		try {
			imgHandler.writeImage(img, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	*Method that will retrieve a hidden message from a PNG.
	*
	*@param			inputFile		Path to the image containing a hidden message
	*@return						The hidden message
	*/
	public String getMessage(String inputFile) {
		ImageHandler imgHandler = new ImageHandler(inputFile);
		//Load PNG as a BufferedImage
		BufferedImage img = imgHandler.getImage();
		return retrieveMessage(img);
	}
	
	/**
	*Method that is used to retrieve a hidden message.
	*<p>
	*Retrieves the least significant bit from each of the RGB values in each pixel to reform the hidden message.
	*
	*@param			image			BufferedImage containing the hidden message
	*@return						The hidden message
	*/
	private String retrieveMessage(BufferedImage image) {
		int x, y;
		int rMask = 1 << 16;
		int gMask = 1 << 8;
		int bMask = 1;
		//Bit masks for the least significant bit for each of the RGB values
		int[] masks = {rMask, gMask, bMask};
		int messageLength = 0;
		int count = 0;
		//Retrieve the length of the hidden message (which is a 32 bit number)
		for(y = 0; y < image.getHeight(); y++) {
			for(x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				for(int i = 0; i < 3; i++) {
					//Get value of bit using the bit masks defined earlier
					int bit = argb & masks[i];
					//Append the bit to the length of the message
					if(bit > 0) {
						messageLength = messageLength | 1;
					}
					count++;
					//Check to see if 32 bits have been retrieved yet
					if(count == 32) {
						break;
					}
					//Shift the message length so that the next bit can be appended
					messageLength = messageLength << 1;
				}
				if(count == 32) {
					break;
				}
			}
			if(count == 32) {
				break;
			}
		}
		//Array to store the retrieved bytes
		byte[] bytes = new byte[messageLength / 8];
		count = 0;
		int byteCounter = 0;
		int bitCounter = 0;
		boolean finished = false;
		for(y = 0; y < image.getHeight(); y++) {
			for(x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				for(int i = 0; i < 3; i++) {
					//Skip the first 32 bits
					if(count > 31) {
						//Get value of bit using the bit masks defined earlier
						int bit = argb & masks[i];
						//Append bit to the current byte that is getting reformed
						if(bit > 0) {
							bytes[byteCounter] = (byte)(bytes[byteCounter] | 1);
						}
						//Keep track of which bit of the byte needs to be retrieved
						bitCounter++;
						if(bitCounter == 8) {
							bitCounter = 0;
							//Move to next byte in array
							byteCounter++;
							//Check to see if the whole message has been retrieved
							if(byteCounter == bytes.length) {
								finished = true;
								break;
							}
						} else {
							//Shift byte so that the next bit can be appended
							bytes[byteCounter] = (byte)(bytes[byteCounter] << 1);
						}
					} else {
						count++;
					}
				}
				if(finished) {
					break;
				}
			}
			if(finished) {
				break;
			}
		}
		//Convert array of bytes to a string
		String message = new String(bytes, Charset.forName("UTF-8"));
		return message;
	}
	
	/**
	*Method used to insert a hidden message into a PNG.
	*<p>
	*Hides a message in the least significant bit of each RGB value of each pixel.
	*
	*@param			image						The BufferedImage that the message will be inserted into
	*@param			message						The message to be hidden
	*@throws		MessageTooLargeException
	*@throws		ImageTooSmallException
	*@return									A BufferedImage containing the hidden message
	*/
	private BufferedImage insertMessage(BufferedImage image, String message) throws MessageTooLargeException, ImageTooSmallException{
		//Convert message (String) into an array of bytes
		byte[] bytes = message.getBytes(Charset.forName("UTF-8"));
		//Get maximum number of bits that can be stored in this image
		long imgMessageBits = (long)image.getHeight() * (long)image.getWidth() * 3;
		int rMask = 1 << 16;
		int gMask = 1 << 8;
		int bMask = 1;
		//Bit masks for the least significant bit for each of the RGB values
		int[] masks = {rMask, gMask, bMask};
		//Check that the length of the message in bits can be stored as a 32 bit number
		if((long)bytes.length * 8 > 4294967295l) {
			throw new MessageTooLargeException("Message must be less than 32^2 - 1 bits");
		} else if(bytes.length * 8 + 32 > imgMessageBits) {
			//Check that image is big enough to store the whole image
			throw new ImageTooSmallException("Image too small to encode entire message");
		} else {
			int messageLength = bytes.length * 8;
			//Bit mask for the message length
			int bitMask = 1 << 31;
			int byteCounter = 0;
			//Bit mask for the byte array (10000000)
			int byteMask = 128;
			boolean finished = false;
			for(int y = 0; y < image.getHeight(); y++) {
				if(finished) {
					break;
				}
				for(int x = 0; x < image.getWidth(); x++) {
					int argb = image.getRGB(x, y);
					for(int i = 0; i < 3; i++) {
						if(finished) {
							break;
						}
						//If the bitMask is not 0 then still need to encode the message length
						if(bitMask != 0) {
							int bit = bitMask & messageLength;
							//If the bit is 1 (bigger than 0 as could be a more significant bit than 2^0)
							if(bit > 0) {
								argb = masks[i] | argb;
							} else {
								argb = (~masks[i]) & argb;
							}
							//Shift bitMask by one to the right so the next bit can be read from the message length
							bitMask = bitMask >>> 1;
							
						} else {
							//If at the end of the current byte
							if(byteMask == 0) {
								//(10000000)
								byteMask = 128;
								//Move to the next byte in the array
								byteCounter++;
								//Check to see if the whole message has been hidden in the image
								if(byteCounter == bytes.length) {
									finished = true;
									break;
								}
							}
							//Get current bit in the byte
							int bit = byteMask & bytes[byteCounter];
							//If the bit is 1 (bigger than 0 as could be a more significant bit than 2^0)
							if(bit > 0) {
								argb = masks[i] | argb;
							} else {
								argb = (~masks[i]) & argb;
							}
							//Shift bitMask by one to the right so the next bit can be read
							byteMask = byteMask >>> 1;
						}
						
					}
					//Update the new rgb value (with part of the message encoded into it)
					image.setRGB(x, y, argb);
				}
			}
		}
		
		return image;
	}
}
