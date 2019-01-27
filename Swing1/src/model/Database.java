package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Database {
	
	
	private List<Person> people;
	private Connection con;
	
	public Database() {
		people = new LinkedList<Person>();
	}
	
	public void connect() throws Exception{
		
		if(con != null) return;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new Exception("Driver not found");
		}
		
		String url = "jdbc:mysql://localhost:3306/swingtest?serverTimezone=UTC";
		con = DriverManager.getConnection(url, "root", "41697414");
		
		System.out.println("Connected: " + con);
	}
	
	public void disconnect() {
		if(con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void save() throws SQLException {
		
		String checkSql = "select count(*) as count from people where id=?";
		
		PreparedStatement checkStmt = con.prepareStatement(checkSql);
		
		String insertSql = "insert into people (id, name, age, employment_status, taxid, us_citizen, gender, occupation) values(?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement insertStatement = con.prepareStatement(insertSql);
		
		String updateSQL = "update people set name = ?, age = ?, employment_status = ?, taxid = ?, us_citizen = ?, gender = ?, occupation = ? where id = ?";
		PreparedStatement updateStmt = con.prepareStatement(updateSQL);
		
		for(Person person: people) {
			int id = person.getId();
			System.out.println("Person id is: " + id);
			String name = person.getName();
			String occupation = person.getOccupation();
			AgeCategory age = person.getAgeCategory();
			EmploymentCategory emp = person.getEmpCat();
			String tax = person.getTaxId();
			boolean isUs = person.isUsCitizen();
			Gender gender = person.getGender();
			
			checkStmt.setInt(1, id);
			
			ResultSet checkResult = checkStmt.executeQuery();
			checkResult.next();
			
			int count = checkResult.getInt(1);
			
			if (count == 0) {
				System.out.println("Inserting person with ID " + id);
				
				int col = 1;
				
				insertStatement.setInt(col++, id);
				insertStatement.setString(col++, name);
				insertStatement.setString(col++, age.name());
				insertStatement.setString(col++, emp.name());
				insertStatement.setString(col++, tax);
				insertStatement.setInt(col++, isUs ? 1 : 0);
				insertStatement.setString(col++, gender.name());
				insertStatement.setString(col++, occupation);

				insertStatement.executeUpdate();
			} else {
				System.out.println("Update person with ID: " + id);
				
				int col = 1;
				
				updateStmt.setString(col++, name);
				updateStmt.setString(col++, age.name());
				updateStmt.setString(col++, emp.name());
				updateStmt.setString(col++, tax);
				updateStmt.setInt(col++, isUs ? 1 : 0);
				updateStmt.setString(col++, gender.name());
				updateStmt.setString(col++, occupation);
				updateStmt.setInt(col++, id);

				updateStmt.executeUpdate();
			}
			
			System.out.println("Count for person with ID" + id + " is " + count);
		}
		
		updateStmt.close();
		insertStatement.close();
		checkStmt.close();
	}
	
	public void load() throws SQLException {
		people.clear();
		
		String sql = "select id, name, age, employment_status, taxid, us_citizen, gender, occupation from people order by name";
		Statement selectStatement = con.createStatement();
		
		ResultSet results = selectStatement.executeQuery(sql);
		
		while(results.next()) {
			int id = results.getInt("id");
			String name = results.getString("name");
			String age = results.getString("age");
			String emp = results.getString("employment_status");
			String tax = results.getString("taxid");
			int isUs = results.getInt("us_citizen");
			String gender = results.getString("gender");
			String occ = results.getString("occupation");
			
			people.add(new Person(id, 
									name, 
									occ, 
									AgeCategory.valueOf(age), 
									EmploymentCategory.valueOf(emp), 
									tax, 
									isUs == 0 ? true : false, 
									Gender.valueOf(gender)));
			
			System.out.println(people.get(people.size() - 1));
		}
		
		results.close();
		selectStatement.close();
	}
	
	public void addPerson(Person person) {
		people.add(person);
	}
	
	public void removePerson(int index) {
		people.remove(index);
	}
	
	public List<Person> getPeople() {
		return Collections.unmodifiableList(people);
	}
	
	public void saveToFile(File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		Person[] persons = people.toArray(new Person[people.size()]);
		
		oos.writeObject(persons);
				
		oos.close();
	}
	
	
	public void loadFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		try {
			Person[] persons = (Person[]) ois.readObject();
			
			people.clear();
			people.addAll(Arrays.asList(persons));
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ois.close();
	}

}
