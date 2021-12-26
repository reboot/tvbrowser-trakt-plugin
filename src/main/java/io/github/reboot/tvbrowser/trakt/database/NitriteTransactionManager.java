package io.github.reboot.tvbrowser.trakt.database;

import org.dizitart.no2.Nitrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@Service
class NitriteTransactionManager extends AbstractPlatformTransactionManager {

    private final Logger logger = LoggerFactory.getLogger(NitriteTransactionManager.class);

    private final NitriteService nitriteService;

    @Autowired
    NitriteTransactionManager(NitriteService nitriteService) {
        this.nitriteService = nitriteService;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new Transaction(nitriteService.getDatabase());
    }

    @Override
    protected void doBegin(Object _transaction, TransactionDefinition definition) throws TransactionException {
        Transaction transaction = (Transaction) _transaction;
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        Transaction transaction = (Transaction) status.getTransaction();

        Nitrite database = transaction.getDatabase();
        if (!database.getContext().isAutoCommitEnabled()) {
            database.commit();
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        Transaction transaction = (Transaction) status.getTransaction();
    }

    private class Transaction {

        private Nitrite database;

        private Transaction(Nitrite database) {
            this.database = database;
        }

        private Nitrite getDatabase() {
            return database;
        }
    }

}
