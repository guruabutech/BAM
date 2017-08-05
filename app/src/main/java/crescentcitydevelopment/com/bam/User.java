package crescentcitydevelopment.com.bam;


import com.google.firebase.database.Exclude;

import java.util.List;

import static android.R.attr.key;

public class User
{
    private String userName;
    private String emailAddress;
    private String userId;

    public User(){}

    public User(String userName, String emailAddress, String userId) {
       this.userName = userName;
        this.emailAddress = emailAddress;
        this.userId = userId;
    }
    public String getUserId(){return userId;}

    public String getEmailAddress() {
        return emailAddress;
    }


    public String getUserName(){return userName;}
}
