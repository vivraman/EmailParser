package emailparser;

import com.auxilii.msgparser.*;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.auxilii.msgparser.attachment.MsgAttachment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class EmailParser {

    String dir;
    File[] files;
    MsgParser msgp;

    public static void main(String[] args) {
        new EmailParser();
    }

    public EmailParser() {
        dir = System.getProperty("user.dir") + "\\emails\\";
        msgp = new MsgParser();
        try {
            files = new File(dir).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().contains(".msg");
                }
            });
            //for each file found in the emails folder:
            for (int i = 0; i < files.length; i++) {
                createMSGFiles(msgp.parseMsg(files[i].getPath()), files[i]);
            }
        } catch (Exception ex) {
            Logger.getLogger(EmailParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createMSGFiles(Message msg, File file) throws IOException, ZipException {
        //make directory for all files corresponding to the .msg:
        File emaildir = new File(file.getParent() + "\\" + file.getName().split("\\.")[0]);
        emaildir.mkdir();
        //make txt file with contents of email:
        File msgbody = new File(emaildir + "\\" + file.getName().split("\\.")[0] + ".rtf");
        msgbody.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(msgbody));
        writer.write(msg.getBodyRTF());
        writer.close();

        //process each alttachment:
        List<Attachment> attachments = msg.getAttachments();
        for (Attachment att : attachments) {
            if (att instanceof FileAttachment) {
                FileAttachment fileatt = (FileAttachment) att;
                File attach = new File(emaildir + "\\" + fileatt.getFilename());
                attach.createNewFile();

                byte[] filedata = fileatt.getData();
                FileOutputStream fos = new FileOutputStream(attach);
                fos.write(filedata);

                if (attach.getName().contains(".zip")) {
                    openZIPFile(attach);
                }
            } else {
                MsgAttachment more = (MsgAttachment) att;
                Message newmsg = more.getMessage();
                File msgdir = new File(emaildir + "\\" + newmsg.getSubject());
                createMSGFiles(newmsg, msgdir);
            }
        }

    }

    private void openZIPFile(File file) throws ZipException {
        ZipFile zipFile = new ZipFile(file.getPath());
        /*if (zipFile.isEncrypted()) {
            zipFile.setPassword(password);
        }*/
        File destination = new File(file.getParent() + "\\" + file.getName().split("\\.")[0]);
        destination.mkdir();
        zipFile.extractAll(destination.getPath());
    }

}
