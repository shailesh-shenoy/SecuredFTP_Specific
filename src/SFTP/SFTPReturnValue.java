package SFTP;

import java.io.Serializable;

public class SFTPReturnValue implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean successful;
	String returnMessage;
	String stackTrace;
	
	public SFTPReturnValue(boolean successful, String returnMessage, String stackTrace)
	{
		super();
		this.successful = successful;
		this.returnMessage = returnMessage;
		this.stackTrace = stackTrace;
	}
	
	public boolean isSuccessful()
	{
		return successful;
	}
	
	public void setSuccessful(boolean successful)
	{
		this.successful = successful;
	}
	
	public String getReturnMessage()
	{
		return returnMessage;
	}
	
	public void setReturnMessage(String returnMessage)
	{
		this.returnMessage = returnMessage;
	}
	
	public String getStackTrace()
	{
		return stackTrace;
	}
	
	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}
	
	@Override
	public String toString()
	{
		return "SFTPReturnValue [successful=" + successful + ", returnMessage=" + returnMessage + ", stackTrace="
				+ stackTrace + "]";
	}
}