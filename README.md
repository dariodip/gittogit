# Git to Git

[![Build Status](https://travis-ci.org/dariodip/gittogit.svg?branch=master)](https://travis-ci.org/dariodip/gittogit)

Una semplice implementazione di un peer git basato su DHT (Tomp2p).
Ogni client può:
1. creare un repository in una directory; 
2. effettuare un commit del repository (se ci sono stati cambiamenti);
3. aggiungere file non ancora tracciati al repository;
4. fare push e pull del proprio repository su DHT. Il sistema notificherà l'utente in caso di conflitto.

- Dario Di Pasquale
- Dario Castellano

## Implementazione

Il progetto ha la seguente struttura:
```
├───resources 
│   └───somefiles
├───src
│   ├───main
│   │   └───java
│   │       └───org
│   │           └───darsquared
│   │               └───gitprotocol
│   │                   ├───dir
│   │                   │   └───exception
│   │                   └───storage
│   └───test
│       └───java
│           └───org
│               └───darsquared
```

### dir
Il package `dir` contiene le classi e le eccezioni che si occupano dell'interazione con la memoria secondaria.

La classe `Repository` astrae il funzionamento del repository: permette di gestire i commit, recuperare informazioni da questi, 
memorizzare e leggere i dati dalla directory principale (necessario per i metodi di *push* e *pull*) ed altre funzioni utili per il funzionamento generale.

La classe `NotADirectoryException` viene lanciata qualora si stesse cercando di creare un repository fornendo come percorso un file.

### storage
Il package `storage` contiene classi e interfacce necessarie per interfacciarsi con la DHT o un altro tipo di storage.

L'interfaccia `Storage` contiene i metodi generici `put` e `get` che, rispettivamente, scrivono e leggono dallo storage.

La classe `DHTStorage` implementa l'interfaccia `Storage` e funziona basandosi sulla DHT offerta dalla libreria TomP2P. 
Il costruttore si occupa di creare (risp. connettersi) alla DHT. I metodi vi accedono, prelevando o inserendo dati.

### gitprotocol

Il package `gitprotocol` contiene i suddetti package e le ulteriori seguenti classi:

La classe `Commit` astrae un commit il cui stato è formato da messaggio, digest del repository, nome del repostory e timestamp.

La classe `OperationMessage` mantiene l'elenco dei possibile messaggi restituiti dai metodi *pull* e *push*.

La classe `GitProtocolImpl` implementa l'inferfaccia `GitProtocol` e gestisce il funzionamento generale del programma. 
I metodi sono stati implementati nel modo seguente:

##### createRepository

Crea un oggetto *Repository* e lo assegna come stato della classe;

##### addFilesToRepository

Aggiunge file non tracciati al repository. La classe *Repository* si occupa di aggiornare tutte le strutture;

##### commit

Aggiunge un nuovo oggetto di tipo Commit all'elenco dei commit della classe *Repository*. Quest'ultima si occupa di gestire
le strutture e il digest. Se il commit viene effettuato su un repository non modificato, restituisce **false**;

##### push

Scarica (se presente) il repository in DHT. Se il digest di questi è presente nella lista dei digest del repository locale,
carica il repository in DHT, altrimenti segnala il conflitto restituendo `Operationmessage.PULL_REQUIRED`.

##### pull

Scarica (se presente) il repository in DHT. Se il repository scaricato non contiene il diget del repository locale, non applica 
le modifiche, ma richiede un ulteriore pull (*fetch*) per modificare i file locali. 
Se non sono stati rilevati conflitti, sostituisce i file locali con quelli presenti nel repository.

## Test
Nel package `test` sono contenute tutte le classi java rappresentanti le suite di test implementate ed i relativi test case

### AppTest
Testa le funzionalità complete dell'implementazione del protocollo git, in particolare testa:
* setup di DHT e GitProtocol;
* creazione di un repository;
* non duplicazione di un repository;
* pull di repository non in DHT;
* pull di repository in DHT;
* push su DHT;
* modifica di un file sotto git;
* aggiunta di file al repository;
* commit.

### GitTest
Testa le funzionalità di gestione di un file sotto git in repository locale, modificandone il contenuto, effettuandone commit e valutando i digest.

### RepositoryTest
Testa le funzionalità di creazione del repository con test sui file e sui digest.
