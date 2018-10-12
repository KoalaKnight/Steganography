import java.lang.Exception;

/**
*ImageTooSmallException is an Exception class used when an image is too small for the entire hidden message to be stored in it.
*
*@author	Toby Flynn
*/
public class ImageTooSmallException extends Exception {
    
    public ImageTooSmallException() {
        super();
    }
    
    public ImageTooSmallException(String msg) {
        super(msg);
    }
}
