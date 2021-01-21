package it.unipi.dii.inginf.lsdb.group9.visiteasy.entities;

public class User {

    private String username;
    private String password;
    private int age;

    //COSTRUTTORI

    public User(String username)
    {
        this.username = username;
    }

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public User(String username, int age)
    {
        this.username = username;
        this.age = age;
    }

    public User(String username, String password, int age)
    {
        this(username, password);
        this.age = age;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public int getAge(){
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
