package com.example.mainactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HMMMainActivity extends AppCompatActivity
{
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    EditText editTextInput, editTextKey, editTextOutput;
    private Context context;

    public static String key;
    public static String initVector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        editTextInput  = (EditText) findViewById(R.id.editTextInput);
        editTextKey  = (EditText) findViewById(R.id.editTextKey);
        editTextOutput  = (EditText) findViewById(R.id.editTextOutput);
    }

    public void clipboardCopy(View view)
    {
        if (editTextOutput.getText().toString().equals(""))
        {
            new AlertDialog.Builder(this).setTitle("Hello!").setMessage("There doesn't seem to be any output. Please try again").setNeutralButton("Okay", null).show();
        }
        else
        {
            setClipboard(editTextOutput.getText().toString());
        }
    }

    private void setClipboard(String text)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
        else
        {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }

        Toast toast = Toast.makeText(context, "Output Copied.", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void performEncrypt(View view)
    {
        try
        {
            if (editTextInput.getText().toString().equals("") || editTextKey.getText().toString().equals(""))
                new AlertDialog.Builder(this).setTitle("Oops...").setMessage("Please make sure to give both an Input and a Key before proceeding.").setNeutralButton("Okay", null).show();

            else
                encryptionController();
        }
        catch (Exception e)
        {
            new AlertDialog.Builder(this).setTitle("Alas!").setMessage("That didn't work. If this error persists, please report it. Error Code: 8080.").setNeutralButton("Okay", null).show();
        }
    }

    public void performDecrypt(View view)
    {
        try
        {

            if (editTextInput.getText().toString().equals("") || editTextKey.getText().toString().equals(""))
                new AlertDialog.Builder(this).setTitle("Oops...").setMessage("Please make sure to give both an Input and a Key before proceeding.").setNeutralButton("Okay", null).show();
            else if (editTextInput.getText().toString().length() <= 16)
                new AlertDialog.Builder(this).setTitle("Oops...").setMessage("Your cipher-text is too short. Please make sure it is the right one and try again.").setNeutralButton("Okay", null).show();
            else
                decryptionController();
        }
        catch(Exception e)
        {
            new AlertDialog.Builder(this).setTitle("Alas!").setMessage("That didn't work. If this error persists, please report it. Error Code: 8888.").setNeutralButton("Okay", null).show();
        }
    }

    public void encryptionController()
    {
        String givenKey = editTextKey.getText().toString();
        key = fixSize(givenKey);

        String originalString = editTextInput.getText().toString();

        initVector = generateIV();

        String encryptedString = encrypt(originalString);
        encryptedString = encryptedString + initVector;

        editTextOutput.setText(encryptedString);
    }

    public String encrypt(String value)
    {
        try {
            IvParameterSpec iv = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            }
            else
            {
                iv = new IvParameterSpec(initVector.getBytes(Charset.forName("UTF-8")));
            }
            SecretKeySpec skeySpec = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            }
            else
            {
                skeySpec = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "AES");
            }

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = new byte[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            }
            else
            {
                encrypted = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
            }

            return bytesToHex(encrypted);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void decryptionController()
    {
        String givenKey = editTextKey.getText().toString();
        key = fixSize(givenKey);

        String cipherString = editTextInput.getText().toString();

        initVector = cipherString.substring(cipherString.length()-16);
        cipherString = cipherString.substring(0, cipherString.length()-16);

        String decryptedString = decrypt(cipherString);

        editTextOutput.setText(decryptedString);
    }

    public String decrypt(String encrypted)
    {
        try {
            IvParameterSpec iv = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            }
            else
            {
                iv = new IvParameterSpec(initVector.getBytes(Charset.forName("UTF-8")));
            }
            SecretKeySpec skeySpec = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            }
            else
            {
                skeySpec = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "AES");
            }

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(hexStringToByteArray(encrypted));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return new String(original, StandardCharsets.UTF_8);
            }
            else
            {
                return new String(original, Charset.forName("UTF-8"));
            }
        } catch (Exception ex)
        {
            new AlertDialog.Builder(this).setTitle("Hmm...").setMessage("Your Key-Cipher combination is incorrect. Please make sure it is correct and try again.").setNeutralButton("Okay", null).show();
        }

        return null;
    }

    public static String generateIV()
    {
        String IV = "";

        Random rand = new Random();

        for (int i = 0; i < 16; ++i)
        {
            IV = IV + BASE.charAt(rand.nextInt(BASE.length()));
        }

        return IV;
    }

    public static String fixSize(String str)
    {
        if (str.length() > 16)
            return str.substring(0, 16);

        while(str.length() < 16)
        {
            str = str + "x";
        }

        return str;
    }

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}