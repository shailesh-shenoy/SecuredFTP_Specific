package SFTP;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPConnection implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String host;
	int port;
	String hostKey;
	String user;
	String password;
	JSch sftpClient;
	Session session;
	ChannelSftp sftpChannel;
	
	SFTPReturnValue createConnection(String host, int port, String hostKey, String user, String password)
	{
		SFTPReturnValue sftpReturnValue = null;
		try
		{
			this.host = host;
			this.port = port;
			this.hostKey = hostKey;
			this.user = user;
			this.password = password;
			this.sftpClient = new JSch();
			// Public key check
			this.sftpClient.setKnownHosts(new ByteArrayInputStream(this.hostKey.getBytes()));
			// Create session
			this.session = sftpClient.getSession(this.user, this.host, this.port);
			this.session.setPassword(this.password);
			this.session.setConfig("StrictHostKeyChecking", "yes");
			// Connect to session
			this.session.connect();
			// Create sftp channel
			this.sftpChannel = (ChannelSftp) this.session.openChannel("sftp");
			// Connect to open channel
			this.sftpChannel.connect();
			sftpReturnValue = new SFTPReturnValue(true, "Connection established successfully", "");
		} catch (Exception e)
		{
			// get stackTrace as string
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();
			String stackTrace = sw.toString();
			
			sftpReturnValue = new SFTPReturnValue(false, "Could not establish connection: " + e.getLocalizedMessage(),
					stackTrace);
			
			// Terminate sessions not created successfully
			if (this.session != null)
			{
				if (this.session.isConnected())
				{
					this.session.disconnect();
					System.out.println("Session terminated");
				}
			}
		}
		
		return sftpReturnValue;
	}
	
	SFTPReturnValue closeConnection()
	{
		SFTPReturnValue sftpReturnValue = null;
		if (this.sftpChannel != null)
		{
			if (this.sftpChannel.isConnected() || this.session.isConnected())
			{
				this.sftpChannel.disconnect();
				this.session.disconnect();
				sftpReturnValue = new SFTPReturnValue(true, "Connection closed successfully", "");
			} else
			{
				sftpReturnValue = new SFTPReturnValue(false, "Connection already disconnected/not established", "");
			}
		} else
		{
			sftpReturnValue = new SFTPReturnValue(false, "Connection does not exist/not initialized", "");
		}
		return sftpReturnValue;
	}
	
	SFTPReturnValue downloadViaSFTP(String downloadFromPath, String downloadToPath)
	{
		SFTPReturnValue sftpReturnValue = null;
		
		// if session exists
		if (this.sftpChannel == null)
		{
			sftpReturnValue = new SFTPReturnValue(false, "Connection not established, create connection first", "");
		} else
		{
			if (this.sftpChannel.isConnected())
			{
				try
				{
					this.sftpChannel.get(downloadFromPath, downloadToPath);
					sftpReturnValue = new SFTPReturnValue(true, "File downloaded successfully from remote path: "
							+ downloadFromPath + " to local path: " + downloadToPath, "");
				} catch (Exception e)
				{
					// get stackTrace as string
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					pw.close();
					String stackTrace = sw.toString();
					
					sftpReturnValue = new SFTPReturnValue(false, "Download failed: " + e.getLocalizedMessage(),
							stackTrace);
				}
			} else
			{
				sftpReturnValue = new SFTPReturnValue(false, "Session disconnected, reconnect to server", "");
			}
		}
		
		return sftpReturnValue;
		
	}
	
	SFTPReturnValue uploadViaSFTP(String uploadFromPath, String uploadToPath)
	{
		SFTPReturnValue sftpReturnValue = null;
		
		// if session exists
		if (this.sftpChannel == null)
		{
			sftpReturnValue = new SFTPReturnValue(false, "Connection not established, create connection first", "");
		} else
		{
			if (this.sftpChannel.isConnected())
			{
				try
				{
					this.sftpChannel.put(uploadFromPath, uploadToPath);
					sftpReturnValue = new SFTPReturnValue(true, "File uploaded successfully from local path: "
							+ uploadFromPath + " to remote path: " + uploadToPath, "");
				} catch (Exception e)
				{
					// get stackTrace as string
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					pw.close();
					String stackTrace = sw.toString();
					
					sftpReturnValue = new SFTPReturnValue(false, "Upload failed: " + e.getLocalizedMessage(),
							stackTrace);
				}
			} else
			{
				sftpReturnValue = new SFTPReturnValue(false, "Session disconnected, reconnect to server", "");
			}
		}
		
		return sftpReturnValue;
		
	}
	
	public SFTPReturnValue securedFileTransfer(String host, int port, String hostKey, String user, String password,
			String transferType, String sourcePath, String targetPath)
	{
		SFTPReturnValue sftpReturnValue;
		
		// Initialize transferType to avoid nullPointerException issues
		if (transferType == null)
			transferType = "";
		
		if (transferType.equalsIgnoreCase("upload") || transferType.equalsIgnoreCase("download"))
		{
			sftpReturnValue = this.createConnection(host, port, hostKey, user, password);
			if (sftpReturnValue.successful)
			{
				if (transferType.equalsIgnoreCase("upload"))
				{
					sftpReturnValue = this.uploadViaSFTP(sourcePath, targetPath);
				} else
				{
					sftpReturnValue = this.downloadViaSFTP(sourcePath, targetPath);
				}
				System.out.println(this.closeConnection());
			}
		}
		
		else
		{
			sftpReturnValue = new SFTPReturnValue(false,
					"please pass valid value in transferTpe parameter: upload or download", "");
		}
		return sftpReturnValue;
	}
	
	public static void main(String args[])
	{
		System.out.println("Test SFTP");
		System.out.println();
		SFTPConnection sftpConnection = new SFTPConnection();
		SFTPReturnValue sftpReturnValue = null;
		
		sftpReturnValue = sftpConnection.securedFileTransfer("192.168.0.100", 22,
				"192.168.0.100 ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAIEA53ay302T9H2S4sF3tg25zISIUnxQh/Pv0xqCakHENIRH8A6Nw4P3A62wt6kVpBGhXJjh7w5P5ZUZ872eicianiaJnwKiA/THxtZSxE5dOh2hRVpCLpWerne3izOL9+wN3obfMj0C+rEoglIK3aLiYm6EYBRQ2zgVoidOt2cJ91U=",
				"test", "Test@123", "download", "/downloads/*", "D:\\sftp\\multiple\\");
		System.out.println("Secured File Transfer: " + sftpReturnValue);
		
//		sftpReturnValue = sftpConnection.createConnection("192.168.0.100", 22,
//				"192.168.0.100 ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAIEA53ay302T9H2S4sF3tg25zISIUnxQh/Pv0xqCakHENIRH8A6Nw4P3A62wt6kVpBGhXJjh7w5P5ZUZ872eicianiaJnwKiA/THxtZSxE5dOh2hRVpCLpWerne3izOL9+wN3obfMj0C+rEoglIK3aLiYm6EYBRQ2zgVoidOt2cJ91U=",
//				"test", "Test@123");
//		System.out.println("Create Connection: \n" + sftpReturnValue);
//		if (sftpConnection.session != null)
//			System.out.println("session connected? : " + sftpConnection.session.isConnected());
//		if (sftpConnection.sftpChannel != null)
//			System.out.println("channel connected? : " + sftpConnection.sftpChannel.isConnected());
		
//		sftpReturnValue = sftpConnection.downloadViaSFTP("/downloads/*.txt", "D:\\sftp\\multiple\\");
//		System.out.println("Download File: \n" + sftpReturnValue);
		
//		sftpReturnValue = sftpConnection.uploadViaSFTP("D:\\sftp\\multiple\\*.txt", "/uploads/");
//		System.out.println("Upload File: \n" + sftpReturnValue);
//		sftpReturnValue = sftpConnection.closeConnection();
		if (sftpConnection.session != null)
			System.out.println("session connected? : " + sftpConnection.session.isConnected());
		if (sftpConnection.sftpChannel != null)
			System.out.println("channel connected? : " + sftpConnection.sftpChannel.isConnected());
//		System.out.println("Close connection: \n" + sftpReturnValue);
	}
	
}
