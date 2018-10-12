import java.lang.Exception;

/**
*MessageTooLargeException is an Exception class used when the length of a message in bits cannot be stored in a 32 bit unsigned number.
*
*@author	Toby Flynn
*/
public class MessageTooLargeException extends Exception {
    
    public MessageTooLargeException() {
        super();
    }
    
    public MessageTooLargeException(String msg) {
        super(msg);
    }
}
