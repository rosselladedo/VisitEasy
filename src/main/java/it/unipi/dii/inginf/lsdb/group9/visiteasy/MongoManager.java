package it.unipi.dii.inginf.lsdb.group9.visiteasy;

import com.mongodb.TransactionOptions;
import com.mongodb.BasicDBObject;

import com.mongodb.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import  java.util.*;
import java.lang.*;

import com.mongodb.client.model.*;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.Administrator;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.Reservation;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.User;
import it.unipi.dii.inginf.lsdb.group9.visiteasy.entities.Doctor;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.print.Doc;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Sorts.descending;


public class MongoManager {

   private final MongoClient mongoClient;
   private final MongoDatabase db;
   private final MongoCollection<Document> users;
   private final MongoCollection<Document> doctors;
   private final MongoCollection<Document> administrators;
   private ClientSession clientSession;


    public MongoManager()
    {
        mongoClient = MongoClients.create();
        db = mongoClient.getDatabase("progetto");
        users = db.getCollection("users");
        doctors = db.getCollection("doctors");
        administrators = db.getCollection("administrator");
    }




    Consumer<Document> printDocuments = document -> {System.out.println(document.toJson());};
    Consumer<Document> printvalue = document -> {System.out.println(document.getString("_id"));};
    Consumer<Document> printnamedoc = document -> {System.out.println(document.getString("name"));};
    Consumer<Document> printnum = document -> {System.out.println(document.getInteger("count"));};
    Consumer<Document> printnum2 = document -> {

        int a = document.getInteger("count");

    };
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader tastiera = new BufferedReader(input);






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
        if (doctors.countDocuments(new Document("username", doctor.getUsername())) == 0) {
            Document doc = new Document("username", doctor.getUsername()).append("password", doctor.getPassword()).append("price", doctor.getPrice()).append("city", doctor.getCity()).append("name", doctor.getName()).append("specialization", doctor.getSpecialization()).append("bio", doctor.getBio()).append("address", doctor.getAddress());
            doctors.insertOne(doc);
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
        Document result = doctors.find(eq("username", doctor.getUsername())).first();
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


    ArrayList<Doctor> cheapestDoc(String city, String specialization)
    {
        ArrayList<Doctor> doclist = new ArrayList<>();

        Consumer<Document> addtolist = document -> {
            Doctor newdoc = new Doctor(document.getString("name"),document.getInteger("price"));
            doclist.add(newdoc);
        };

        Bson myMatch = match(and(eq("city",city), eq("specialization", specialization), gte("price", 1)));
        Bson mySort = sort(ascending("price"));
        Bson myLimit = limit(3);

        doctors.aggregate(Arrays.asList(myMatch,mySort,myLimit)).forEach(addtolist);
        return doclist;
    }

    Doctor getDocInfo(String name)
    {
        Document result = doctors.find(eq("name", name)).first();
        Doctor doctor = new Doctor(result.getString("name"),result.getInteger("price"),result.getString("address"),result.getString("bio"));
        return  doctor;
    }

    Doctor getMyProfile(String username)
    {
        Document result = doctors.find(eq("username", username)).first();
        Doctor doctor = new Doctor(result.getString("name"),result.getInteger("price"),result.getString("address"),result.getString("bio"));
        return  doctor;
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

    /* Add reservations to the doctor with username = us from a date chosen by the doctor fino a 1 anno o quello che è     ( tutte le date hanno orari uguali che sceglie il dottore) */


 /* Elimina tutte le prenotazioni il cui giorno rientra in un range di date, dal calendario dei dottori e dalle prenotazioni degli utenti*/
    void deleteReservation()
    {
        DateTime start = DateTime.now().minusDays(2);
        DateTime end = start.plusDays(1);

        List<DateTime> between = getDateRange(start, end);

        for (DateTime d : between)
        {
            Bson delete = Updates.pull("calendar", new Document("date", d.toString(DateTimeFormat.shortDate())));
          //  System.out.println(d.toString(DateTimeFormat.shortDate())+" 15.00");
            Bson deleteu = Updates.pull("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())));
            doctors.updateMany(new Document(), delete);
            users.updateMany(new Document(),deleteu);
        }
    }

    void deleteReservation2()
    {
        DateTime start = DateTime.now().minusDays(2);
        DateTime end = start.plusDays(1);

        List<DateTime> between = getDateRange(start, end);

        ArrayList<String> orari = new ArrayList<>();
        orari.add(" 15:00");
        orari.add(" 16:30");

        //List<String> ore = null;
        //ore.add("15.00");

        for (DateTime d : between)
        {
            for (String o: orari)
            {
                Bson delete = Updates.pull("calendar", new Document("date", d.toString(DateTimeFormat.shortDate())+o));
                 // System.out.println(d.toString(DateTimeFormat.shortDate())+o);
               // Bson deleteu = Updates.pull("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())));
                doctors.updateMany(new Document(), delete);
              //  users.updateMany(new Document(), deleteu);
            }
        }
    }

    boolean trovato(String namedoc,String date,String patient)
    {
        Bson match = match(eq("name",namedoc));
        Bson unwind = unwind("$reservations");
        Bson match2 = match(and(eq("reservations.date",date),eq("reservations.patient",patient)));
        Bson count = count();

        int b;
        try
        { b = doctors.aggregate(Arrays.asList(match, unwind, match2, count)).first().getInteger("count");
            System.out.println(b);
        }catch (NullPointerException e){
            System.out.println("NON HO TROVATO NIENTE");
            return false;
        }
        System.out.println("HO TROVATO");
        return true; //ha trovato quello che gli passo nei parametri

    }

    /* Libera una prenotazione*/
    void freeSlot(Reservation reservation)
    {
        String doctor = reservation.getDocname();
        String date = reservation.getDate();
        String user = reservation.getUsername();

        /* Mi assicuro che lo slot che voglio eliminare sia proprio quello dell'utente che ha fatto il login*/
        if (trovato(doctor,date,user))
        {
            clientSession = mongoClient.startSession();

            TransactionBody transactionBody = new TransactionBody<String>() {
                @Override
                public String execute() {

                    doctors.updateOne(new Document("name",doctor).append("reservations.date",date),Updates.set("reservations.$.patient",""));

                    Bson match = new Document("username",user);
                    Bson deleteu = Updates.pull("reservations", new Document("date", date));
                    users.updateOne(match,deleteu);
                    System.out.println("Reservation deleted");
                    return null;
                }
            };

            try {

                clientSession.withTransaction(transactionBody);

            }catch (RuntimeException e){
                System.out.println("ERROR: Operation not performed");
            }finally {
                clientSession.close();
            }

        }else {
            System.out.println("ERROR: There is not present any your reservation in the selected slot");
        }
    }



    /*se lo slot è libero inserisco l'appuntamento sia nella collection doctors che in users */
    boolean book(Reservation reservation)
    {
        String user = reservation.getUsername();
        String date = reservation.getDate();
        String name = reservation.getDocname();

        if (users.countDocuments(new Document("username",user).append("reservations.date",date)) == 0)
        {
            if (trovato(name, date, ""))
            {
               clientSession = mongoClient.startSession();

                TransactionBody transactionBody = new TransactionBody<String>() {
                    @Override
                    public String execute() {

                        doctors.updateOne(new Document("name", name).append("reservations.date", date), Updates.set("reservations.$.patient", user));

                        Document newres = new Document("date", date).append("doctor", name);
                        users.updateOne(eq("username", user), Updates.push("reservations", newres));
                        return null;
                    }
                };

                try {

                    clientSession.withTransaction(transactionBody);

                }catch (RuntimeException e){
                    System.out.println("ERROR: reservation not done");
                    return false;
                }finally {
                    clientSession.close();
                }


                System.out.println("Reservation made! :)");
                return true;
            } else {
                System.out.println("We're sorry :( , the slot is already occupied by another patient, please choose another one.");
                return false;
            }
        }else {
            System.out.println("ERROR: You already have a reservation in this datetime");
            return false;
        }
    }

    void deleteDate (){

        ArrayList<String> orari = new ArrayList<>();
        orari.add(" 09:00");
        orari.add(" 09:30");
        orari.add(" 10:00");
        orari.add(" 10:30");
        orari.add(" 11:00");
        orari.add(" 11:30");
        orari.add(" 12:00");
        orari.add(" 12:30");
        orari.add(" 15:00");
        orari.add(" 15:30");
        orari.add(" 16:00");
        orari.add(" 16:30");
        orari.add(" 17:00");
        orari.add(" 17:30");
        orari.add(" 18:00");
        orari.add(" 18:30");


        DateTime start = DateTime.now().minusDays(2);
        DateTime end = start.plusDays(1);
        List<DateTime> between = getDateRange(start, end);


        for (DateTime d : between)
        {
            for (String o : orari)
            {
                Bson delete = Updates.pull ("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())+o));

                //Bson delete = delete;
                doctors.updateMany(new Document(), delete);
                //users.updateMany(new Document(),delete);

            }
            //Bson delete = Updates.pull("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())));
            //doctors.updateMany(new Document(), delete);
            //users.updateMany(new Document(),delete);
        }





    }

    /* Elimina tutte le prenotazioni il cui giorno rientra in un range di date, dal reservations dei dottori e dalle prenotazioni degli utenti*/
    void deleteReservation(String username)
    {

        ArrayList<String> orari = new ArrayList<>();
        orari.add("09:00");
        orari.add("09:30");
        orari.add("10:00");
        orari.add("10:30");
        orari.add("11:00");
        orari.add("11:30");
        orari.add("12:00");
        orari.add("12:30");
        orari.add("15:00");
        orari.add("15:30");
        orari.add("16:00");
        orari.add("16:30");
        orari.add("17:00");
        orari.add("17:30");
        orari.add("18:00");
        orari.add("18:30");
        System.out.println("insert how many days do you want to delete");
        int day = keyboard.nextInt();
        DateTime start = DateTime.now();
        DateTime end = start.plusDays(day);

        List<DateTime> between = getDateRange(start, end);


        for (DateTime d : between)
        {
            for (String o : orari)
            {
                Bson delete = new Document ("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())+" "+o).append("patient",""));
                System.out.println(delete);
               // Bson delete = delete;
                doctors.updateMany(new Document(), delete);
                //users.updateMany(new Document(),delete);

            }
            //Bson delete = Updates.pull("reservations", new Document("date", d.toString(DateTimeFormat.shortDate())));
            //doctors.updateMany(new Document(), delete);
            //users.updateMany(new Document(),delete);
        }
    }
    //aggiunge tutto il calendario a tutti i dottori
    void aggiungi_cal7()
    {
        ArrayList<String> ore = new ArrayList<>();
        ore.add("09:00");
        ore.add("11:00");
        ore.add("15:00");
        ore.add("16:30");

        DateTime start = DateTime.now();
        DateTime end = start.plusMonths(2);

        List<DateTime> between = getDateRange(start, end);

        for (DateTime d : between)
        {
            for (String o : ore)
            {
                Document newres = new Document("date",d.toString(DateTimeFormat.shortDate())+" "+o).append("patient","");
                doctors.updateMany(new Document(),Updates.push("reservations",newres));
            }
        }

    }

    //genera calendario dottore inserendo giorni da aggiungere e orari da aggiungere
    void aggiungi_cal4(String username) throws IOException {
        ArrayList<String> ore = new ArrayList<>();
        System.out.println("insert how many hours do you want to add");
        int hour = keyboard.nextInt();
        for(int i=0; i<hour; i++){
            System.out.println("insert the hour you want to add");
            String orario = tastiera.readLine();
            ore.add(orario);
        }

        System.out.println("insert how many days you want add:");
        int day = keyboard.nextInt();

        DateTime start = DateTime.now();
        DateTime end = start.plusDays(day);

        List<DateTime> between = getDateRange(start, end);

        for (DateTime d : between)
        {
            for (String o : ore)
            {
                Document newres = new Document("date",d.toString(DateTimeFormat.shortDate())+" "+o).append("patient","");
                doctors.updateMany(new Document("username",username),Updates.push("reservations",newres));
            }
        }

    }
    //inserisce nuovo orario al calendario dottore

    void insert_hour_doctor_reservations(String username) throws IOException {

        System.out.println("insert the date you want to update:");
        String date = tastiera.readLine();
        System.out.println("insert the new hour:");
        String orario = tastiera.readLine();


        Document newres = new Document("date",date +" "+orario).append("patient","");
        doctors.updateOne(new Document("username",username),Updates.push("reservations",newres));
        System.out.println(newres);
    }



    void show_day(String name, String day )
    {
        Bson match = match(eq("name",name));
        Bson unwind = unwind("$reservations");
        Bson project = project(fields(excludeId(), include("reservations")));
        Bson match2 = match(eq("reservations.date",day));

        doctors.aggregate(Arrays.asList(match,unwind,project,match2)).forEach(printDocuments);
    }



    /* Mostra tutti gli slot disponibili di un dottore */
    ArrayList<Reservation> showEntirereservations(String name)
    {
        ArrayList<Reservation> datelibere = new ArrayList<>();
        Consumer<Document> printcale = document -> {

            Reservation reservation = new Reservation(name,document.getEmbedded(Arrays.asList("reservations", "date"), String.class));
            datelibere.add(reservation);
        };

        Bson match = match(eq("name",name));
        Bson unwind = unwind("$reservations");
        Bson match2 = match(eq("reservations.patient",""));
        Bson project = project(fields(excludeId(), include("reservations")));

        doctors.aggregate(Arrays.asList(match,unwind,match2,project)).forEach(printcale);
        return datelibere;
    }


    /* Mostra il reservations con le prenotazioni di un dottore */
    void showreservations(String username) {

        Bson match = match(eq("username",username));
        Bson unwind = unwind("$reservations");
        Bson project = project(fields(excludeId(), include("reservations")));
        doctors.aggregate(Arrays.asList(match,unwind,project)).forEach(printDocuments);
    }

    void showUserReservations(String user)
    {
        Bson match = match(eq("username",user));
        Bson unwind = unwind("$reservations");
        Bson project = project(fields(excludeId(), include("reservations")));

        users.aggregate(Arrays.asList(match,unwind,project)).forEach(printDocuments);
    }



    //Analytics svg users
    void printAvgUsers(){
        AggregateIterable<org.bson.Document> aggregate = users.aggregate(Arrays.asList(Aggregates.group("_id", new BsonField("averageAge", new BsonDocument("$avg", new BsonString("$age"))))));
        Document result = aggregate.first();
        double age = result.getDouble("averageAge");
        System.out.println("the average of the age of the user is: ");
        System.out.println(age);

    }
    //Analytics most expensive doctors
    void printMostExpSpec(){

        Bson group1 = new Document("$group", new Document("_id", new Document("specialization", "$specialization"))
                .append("AvgPrice", new Document("$avg", "$price")));
        Bson project1 = project(fields(excludeId(), computed("specialization", "$_id.specialization"),
                include("AvgPrice")));


        Bson sort = sort(descending("AvgPrice"));
        Bson limit = limit(3);
        System.out.println("The three most expensive specialization are: ");
        doctors.aggregate(Arrays.asList(group1, project1, sort, limit))
                .forEach(printDocuments);

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