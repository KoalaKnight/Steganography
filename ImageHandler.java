import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
*ImageHandler is a class that can load or save a PNG image (as a BufferedImage).
*
*@author	Toby Flynn
*/
public class ImageHandler {
	private BufferedImage image = null;
	
	/**
	*Constructor method for ImageHandler that loads an image.
	*
	*@param		filename	the filename (or path) of the image
	*/
	public ImageHandler(String filename) {
		try {
			File file = new File(filename);
			image = ImageIO.read(file);
		} catch (Exception e) {
			System.out.println("Error when reading image: ");
			e.printStackTrace();
		}
	}
	
	/**
	*Default constructor method for ImageHandler.
	*/
	public ImageHandler() {
	
	}
	
	/**
	*Getter method for the image that was loaded in the constructor.
	*
	*@return				the BufferedImage that was loaded in the constructor
	*/
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	*Writes a BufferedImage as a PNG to a specified location.
	*
	*@param        image       A BufferedImage that will be saved
	*@param        filename    The filename that the image will be written to
	*/
	public void writeImage(BufferedImage image, String filename) throws IOException {
        ImageIO.write(image, "png", new File(filename));
	}
}
