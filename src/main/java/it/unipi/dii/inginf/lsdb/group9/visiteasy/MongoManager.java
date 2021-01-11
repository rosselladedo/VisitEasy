package it.unipi.dii.inginf.lsdb.group9.visiteasy;

import com.mongodb.client.*;

import  java.util.*;
import java.lang.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Updates;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.Administrator;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.User;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.Doctor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;


public class MongoManager {

    MongoClient mongoClient = MongoClients.create();
    MongoDatabase db = mongoClient.getDatabase("progetto");
    MongoCollection<Document> users = db.getCollection("users");
    MongoCollection<Document> doctors = db.getCollection("doctors");
    MongoCollection<Document> administrators = db.getCollection("administrator");

    Consumer<Document> printDocuments = document -> {System.out.println(document.toJson());};
    Consumer<Document> printvalue = document -> {System.out.println(document.getString("_id"));};
    Consumer<Document> printnamedoc = document -> {System.out.println(document.getString("name"));};


    Scanner keyboard = new Scanner(System.in);


    boolean add_user(User user)
    {
        if (users.countDocuments(new Document("username", user.getUsername())) == 0) {
            Document doc = new Document("username", user.getUsername()).append("password", user.getPassword()).append("age", user.getAge());
            users.insertOne(doc);
            return true;
        } else return false;
    }

    boolean add_doctor(Doctor doctor)
    {
        if (users.countDocuments(new Document("username", doctor.getUsername())) == 0) {
            Document doc = new Document("username", doctor.getUsername()).append("password", doctor.getPassword()).append("name", doctor.getName()).append("city", doctor.getCity()).append("bio", doctor.getBio()).append("specialization", doctor.getSpecialization()).append("address", doctor.getAddress()).append("price", doctor.getPrice());
            users.insertOne(doc);
            return true;
        } else return false;
    }


    boolean login_user(User user)
    {
        Document result = users.find(eq("username", user.getUsername())).first(); //salvo in "result" il documento il cui campo username è uguale a quello passato come parametro
        try {
            result.getString("username");
        }catch (NullPointerException exception){
            System.out.println("The username does not exist");
            return false;
        }

        String psw = result.getString("password");
        if (psw.equals(user.getPassword())) {
            int age = result.getInteger("age");
            user.setAge(age);
            System.out.println("Correct credentials");
            return true;
        } else{
            System.out.println("Incorrect password");
            return false;
        }

    }


    boolean login_doctor(Doctor doctor)
    {
        Document result = users.find(eq("username", doctor.getUsername())).first();
        try {
            result.getString("username");
        }catch (NullPointerException exception){
            System.out.println("The username does not exist");
            return false;
        }

        String psw = result.getString("password");
        if (psw.equals(doctor.getPassword())) {
            System.out.println("Correct credentials");
            return true;
        } else{
            System.out.println("Incorrect password");
            return false;
        }

    }


    boolean login_administrator(Administrator administrator){
        Document result = administrators.find(eq("username", administrator.getUsername())).first(); //salvo in "result" il documento il cui campo username è uguale a quello passato come parametro
        try {
            result.getString("username");
        }catch (NullPointerException exception){
            System.out.println("The username does not exist");
            return false;
        }
        String psw = result.getString("password");
        if (psw.equals(administrator.getPassword())) {
            System.out.println("Correct credentials");
            return true;}
        else{
            System.out.println("Incorrect password");

        return false;}
    }

    // ADD ADMINISTRATOR BY ADMINISTRATOR
boolean add_administrator(Administrator administrator) {

    if (administrators.countDocuments(new Document("username", administrator.getUsername())) == 0) {
        Document doc = new Document("username", administrator.getUsername()).append("password", administrator.getPassword());
        administrators.insertOne(doc);
        return true;
    } else return false;

}



    void display_cities() //stampa tutte le città presenti nel DB
    {
        Bson myGroup = Aggregates.group("$city");
        doctors.aggregate(Arrays.asList(myGroup)).forEach(printvalue);
    }

    void display_spec() //stampa tutte le specializzazioni dei medici presenti nel DB
    {
        Bson myGroup = Aggregates.group("$specialization");
        doctors.aggregate(Arrays.asList(myGroup)).forEach(printvalue);
    }


    ArrayList<Doctor> getDocByCitySpec(String city, String specialization)
    {
        ArrayList<Doctor> doclist = new ArrayList<>();

        Consumer<Document> addtolist = document -> {
            Doctor newdoc = new Doctor(document.getString("name"));
            doclist.add(newdoc);
        };

        doctors.find(and(eq("city", city), eq("specialization", specialization))).forEach(addtolist);
        return doclist;
    }


//DELETE USER
    boolean delete_user_by_the_administrator(User user)
    {
        Document result = users.find(eq("username", user.getUsername())).first();
        try {
            result.getString("username");
        } catch (NullPointerException exception) {
            System.out.println("The username does not exist");
            return false;}
        users.deleteOne(eq("username",user.getUsername()));
        return true;
    }
    //DELETE DOCTOR
    boolean delete_doctor_by_the_administrator(Doctor doctor)
    {
        Document result = users.find(eq("username", doctor.getUsername())).first();
        try {
            result.getString("username");
        } catch (NullPointerException exception) {
            System.out.println("The username does not exist");
            return false;}
        users.deleteOne(eq("username",doctor.getUsername()));
        return true;
    }

      /* Restituisce una lista di date dalla data start a quella di end*/
    public static List<DateTime> getDateRange(DateTime start, DateTime end)
    {
        List<DateTime> ret = new ArrayList<DateTime>();
        DateTime tmp = start;
        while (tmp.isBefore(end) || tmp.equals(end)) {
            ret.add(tmp);
            tmp = tmp.plusDays(1);
        }
        return ret;
    }

     /* Aggiunge il calendario al dottore che ha username = us dalla data che decide il dottore fino a 1 anno o quello che è     ( tutte le date hanno orari uguali che sceglie il dottore) */
    void aggiungi_cal3(String us, String ora1, String ora2, String ora3, String start_date)
    {
       // DateTime start = DateTime.now().withTimeAtStartOfDay();
        DateTime start = DateTime.parse(start_date);
        DateTime end = start.plusYears(1);

        List<DateTime> between = getDateRange(start, end);

        for (DateTime d : between)
        {
            Document newdoc = new Document("date", d.toString(DateTimeFormat.shortDate())).append(ora1,"").append(ora2,"").append(ora3,"");
            doctors.updateMany(eq("username",us), Updates.push("calendario",newdoc));
        }
    }

    /*aggiunge un nuovo orario al calendario del dottore */
    void aggiungi_ora(String username, String date, String newhour)
    {
        Document query = new Document("username",username).append("calendario.date",date);

        Document updateQuery = new Document();
        updateQuery.put("calendario.$."+newhour,"");
        doctors.updateOne(query,new Document("$set",updateQuery));
    }




    //ADD NEW DOCTOR BY ADMINISTRATOR
   /* boolean add_new_doctor_by_administrator(Doctor doctor)
    {
        if (users.countDocuments(new Document("username", user.getUsername())) == 0) {
            Document doc = new Document("username", user.getUsername()).append("password", user.getPassword()).append("age", user.getAge());
            users.insertOne(doc);
            return true;
        } else return false;
    }

    /*void populate_doctors_from_file(String path)
    {

        List<Document> observationDocuments = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path));) {
            String line;
            while ((line = br.readLine()) != null) {
                observationDocuments.add(Document.parse(line));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        doctors.insertMany(observationDocuments);
    }


    void populate_users_from_file(String path)
    {

        List<Document> observationDocuments = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path));) {
            String line;
            while ((line = br.readLine()) != null) {
                ((ArrayList<?>) observationDocuments).add(Document.parse(line));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        users.insertMany(observationDocuments);
    }

    public void populate_doctors_from_file() {
    }
    public void populate_users_from_file() {
    }*/
    }
