package edu.mum.customer.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.mum.customer.domain.User;
import edu.mum.customer.domain.UserProfile;

@Transactional(propagation=Propagation.REQUIRED)
public class UserDao implements IUserDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void saveUser(User user, UserProfile userProfile) {
		sessionFactory.getCurrentSession().persist(user);
		sessionFactory.getCurrentSession().persist(userProfile);
		
	}
	
	public User getUser(Long userId) {
		// TODO Auto-generated method stub
		return (User) sessionFactory.getCurrentSession().get( User.class, userId);
	}
	
}