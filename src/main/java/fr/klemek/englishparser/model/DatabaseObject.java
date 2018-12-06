package fr.klemek.englishparser.model;

import fr.klemek.logger.Logger;
import fr.klemek.englishparser.utils.DatabaseManager;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.MappedSuperclass;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@MappedSuperclass
public abstract class DatabaseObject implements Serializable {

    protected DatabaseObject() {
    }

    public boolean save() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot save object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.save(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public boolean update() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot update object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.update(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public boolean delete() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot delete object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.delete(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    protected static <T> List<T> getAll(Class<T> objectClass) {
        return DatabaseManager.getRowsFromSessionQuery("FROM " + objectClass.getSimpleName());
    }
}
