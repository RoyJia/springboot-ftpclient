package cu.yoandypv.ftp.ftpclient;

import cu.yoandypv.ftp.exceptions.ErrorMessage;
import cu.yoandypv.ftp.exceptions.FTPErrors;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;


@Service
public class FTPServiceImpl implements FTPService {

    /**
     * FTP connection handler
     */
    FTPClient ftpconnection;

    private Logger logger = LoggerFactory.getLogger(FTPServiceImpl.class);

    /**
     * Method that implement FTP connection.
     * @param host IP of FTP server
     * @param user FTP valid user
     * @param pass FTP valid pass for user
     * @throws FTPErrors Set of possible errors associated with connection process.
     */
    @Override
    public void connectToFTP(String host, String user, String pass) throws FTPErrors {

        ftpconnection = new FTPClient();
        ftpconnection.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;

        try {
            ftpconnection.connect(host);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage(-1, "It was not possible to connect to FTP through the host =" + host);
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }

        reply = ftpconnection.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {

            try {
                ftpconnection.disconnect();
            } catch (IOException e) {
                ErrorMessage errorMessage = new ErrorMessage(-2, "It was not possible to connect to FTP, the host =" + host + " delivered the answer =" + reply);
                logger.error(errorMessage.toString());
                throw new FTPErrors(errorMessage);
            }
        }

        try {
            ftpconnection.login(user, pass);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage(-3, "The user =" + user + ", and the pass = **** were not valid for authentication.");
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }

        try {
            ftpconnection.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage(-4, "The data type for the transfer is not valid.");
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }

        ftpconnection.enterLocalPassiveMode();
    }

    /**
     * Method that allow upload file to FTP
     * @param file File object of file to upload
     * @param ftpHostDir FTP host internal directory to save file
     * @param serverFilename Name to put the file in FTP server.
     * @throws FTPErrors Set of possible errors associated with upload process.
     */
    @Override
    public void uploadFileToFTP(File file, String ftpHostDir, String serverFilename) throws FTPErrors {

        try {
            InputStream input = new FileInputStream(file);
            this.ftpconnection.storeFile(ftpHostDir + serverFilename, input);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage(-5, "The file could not be uploaded to the server.");
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }

    }

    /**
     * Method for download files from FTP.
     * @param ftpRelativePath Relative path of file to download into FTP server.
     * @param copytoPath Path to copy the file in download process.
     * @throws FTPErrors Set of errors associated with download process.
     */

    @Override
    public void downloadFileFromFTP(String ftpRelativePath, String copytoPath) throws FTPErrors {

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(copytoPath);
        } catch (FileNotFoundException e) {
            ErrorMessage errorMessage = new ErrorMessage(-6, "Could not get the reference to the relative folder to save, verify the path and permissions.");
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }

        try {
            this.ftpconnection.retrieveFile(ftpRelativePath, fos);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage(-7, "The file could not be downloaded.");
            logger.error(errorMessage.toString());
            throw new FTPErrors(errorMessage);
        }
    }

    /**
     * Method for release the FTP connection.
     * @throws FTPErrors Error if unplugged process failed.
     */
    @Override
    public void disconnectFTP() throws FTPErrors {
        if (this.ftpconnection.isConnected()) {
            try {
                this.ftpconnection.logout();
                this.ftpconnection.disconnect();
            } catch (IOException f) {
               throw new FTPErrors( new ErrorMessage(-8, "An error occurred when disconnecting the FTP server."));
            }
        }
    }

    public boolean deleteFile(String filePath) throws FTPErrors {
        boolean reply = false;
        try {
            replay = this.ftpconnection.deleteFile(filePath);
        } catch (Exception e) {
            throw new FTPErrors( new ErrorMessage(-9, "An error occurred when delete file=" + filePath));
        }

        return reply;
    }
}
