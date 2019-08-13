package fr.klemek.englishparser.model;

import fr.klemek.englishparser.utils.DatabaseManager;
import fr.klemek.logger.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

@MappedSuperclass
public abstract class DatabaseObject implements Serializable {

    protected DatabaseObject() {
    }

    public boolean save() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot save object");
            return false;
        }
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        if (session.getTransaction().isActive()) {
            session.save(this);
            return true;
        }
        Transaction tx = null;
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
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        if (session.getTransaction().isActive()) {
            session.update(this);
            return true;
        }
        Transaction tx = null;
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
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        if (session.getTransaction().isActive()) {
            session.delete(this);
            return true;
        }
        Transaction tx = null;
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
