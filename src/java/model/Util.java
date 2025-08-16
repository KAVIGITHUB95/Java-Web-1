package model;

public class Util {

    public static String generateCode() {

        int r = (int) (Math.random() * 100000);

        return String.format("%06d", r);
    }

    public static String generateVerificationEmail(String recipientName, String verificationCode) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='en'>");
        sb.append("<head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>Email Verification</title>");
        sb.append("</head>");
        sb.append("<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>");

        sb.append("<div style='max-width: 600px; margin: auto; background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>");
        sb.append("<h2 style='color: #333;'>Hello, ").append(recipientName).append("!</h2>");
        sb.append("<p style='font-size: 16px; color: #555;'>Thank you for registering. Please use the verification code below to complete your sign-up process:</p>");

        sb.append("<div style='margin: 20px 0; text-align: center;'>");
        sb.append("<span style='display: inline-block; background-color: #007bff; color: #fff; padding: 10px 20px; font-size: 24px; letter-spacing: 3px; border-radius: 5px;'>")
                .append(verificationCode)
                .append("</span>");
        sb.append("</div>");

        sb.append("<p style='font-size: 14px; color: #777;'>If you did not request this, please ignore this email.</p>");
        sb.append("<p style='font-size: 14px; color: #777;'>Regards,<br><strong>Your Company Team</strong></p>");
        sb.append("</div>");

        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static boolean isEmailValidation(String email) {
        return email.matches("^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    public static boolean isPasswordValid(String password) {

        return password.matches("^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$");
    }

    public static boolean isCodeValid(String code) {

        return code.matches("^\\d{4,5}$");
    }

    public static boolean isInteger(String value) {
        return value.matches("^\\d+$");

    }

    
    public static boolean isMobileValid(String mobile) {
        return mobile.matches("^(?:\\+94|94|0)(70|71|72|74|75|76|77|78)\\d{7}$");
    }

    public static boolean isNameValid(String name) {
        return name.matches("^[A-Za-z][A-Za-z\\s'-]{1,49}$"); // 2-50 characters, allows spaces and apostrophes
    }
    
    
    public static boolean isAddressLineValid(String line) {
        return line != null && line.matches("^[\\w\\s,.'-/]{3,100}$");
    }
    
    public static boolean isDouble(String value){
        return value.matches("^[+-]?(\\d*\\.\\d+|\\d+\\.\\d*)([eE][+-]?\\d+)?$");
    
    }
}
