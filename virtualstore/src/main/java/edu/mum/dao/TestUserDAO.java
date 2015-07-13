package edu.mum.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.mum.customer.domain.User;

@Transactional(propagation=Propagation.REQUIRED)
public class TestUserDAO {
	
	@Autowired
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void init() 
	{
		
		//User u1=new User("Avishek","mypass");
		
		//sessionFactory.getCurrentSession().persist(u1);
		
		
	}
	
	public User getSampleUser()
	{
		User u=new User("test","mypass");
		
		return u;
	}
	
	public void saveUser(User u)
	{
		sessionFactory.getCurrentSession().persist(u);
	}

}