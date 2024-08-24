package utils;

import controllers.GUIManager;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    public static String importImageFromBase64(String base64ImageContents, String filePath) {       //imports an image from Base64 and returns its filepath
        if (base64ImageContents == null)
            return null;

        byte[] imageBytes = Base64.getDecoder().decode(base64ImageContents);    //convert from Base 64
        return saveImage(imageBytes, filePath);
    }

    public static String saveImage(byte[] imageFileContent, String filePath) {
        try {
            filePath = convertToSafeFileName(filePath);                         //rename file if necessary
            filePath = appendNumberIfDupe(filePath);
            FileUtils.writeByteArrayToFile(new File(filePath), imageFileContent);   //and save it
            return filePath;
        } catch (IOException e) {
            System.err.println("Could not create file.");
            return null;
        }
    }

    public static String convertToSafeFileName(String fullPath) {       //makes file name safe (tested on Windows)
        int indexOfFileName = fullPath.lastIndexOf("/") + 1;
        String outPath = fullPath.substring(0, indexOfFileName);
        String fileName = fullPath.substring(indexOfFileName);
        fileName = fileName.replaceAll("[\\s]", "_")    //spaces and parentheses can break CSS
                .replaceAll("[()]", "")
                .replaceAll("[/:*?\"<>|\\\\]", "");     //illegal characters for file name

        if (fileName.isEmpty())                                         //empty is technically legal but an uninspiring default name
            fileName = "New_File";
        else if (fileName.length() > 255)                               //max length is either 255 or within 5 chars over it
            fileName = fileName.substring(0, 255);
        return outPath + fileName;
    }

    public static String appendNumberIfDupe(String filePath) {
        if (!Files.exists(Paths.get(filePath)))                         //quick check for the most common case, no dupes
            return filePath;

        String name = filePath.substring(0, filePath.lastIndexOf("."));
        String extension = filePath.substring(filePath.lastIndexOf("."));
        if (!name.matches(".+_\\d+"))                              //if file doesn't have a number appended yet, add one
            name = name + "_1";

        int indexOfNumber;
        while (Files.exists(Paths.get(name + extension))) {         //increment number at end of file name until it's no longer a duplicate
            indexOfNumber = name.lastIndexOf("_") + 1;
            name = name.substring(0, indexOfNumber) + (Integer.parseInt(name.substring(indexOfNumber)) + 1);        //set name to what it was but increment the number at the end
        }

        return name + extension;
    }

    public static File saveFileChooser(String defaultName) {        //creates a FileChooser in save mode
        FileChooser fileChooser = getFileChooser();
        fileChooser.setInitialFileName(ImageUtils.convertToSafeFileName(defaultName));
        fileChooser.setTitle("Save image");
        return fileChooser.showSaveDialog(GUIManager.mainStage);
    }

    public static File openFileChooser() {                          //creates a FileChooser in open mode
        FileChooser fileChooser = getFileChooser();
        fileChooser.setTitle("Upload image");
        return fileChooser.showOpenDialog(GUIManager.mainStage);
    }

    private static FileChooser getFileChooser() {                   //passes a FileChooser with the formats we support to above methods
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.wbmp"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"), new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"), new FileChooser.ExtensionFilter("WBMP", "*.wbmp"));

        return fileChooser;
    }
}
