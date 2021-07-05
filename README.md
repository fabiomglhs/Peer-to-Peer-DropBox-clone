# Peer-to-Peer Dropbox Clone

In this project was developed a distributed DropBox clone service for a local area network (LAN). The idea is to use the free disk space of the computers in a LAN for replicating files in other computers in the same LAN. Each node (“peer”) is responsible for its own disks and, if needed, may reclaim space.



## 1.

Open a terminal, go to Docker folder inside the project and start gossip-router

*docker-compose up gossip-router*


## 2.

Open another terminal , go to the Docker folder inside the project and start the peers

*docker-compose up peer-box*

## 3.

Run the program in the IDE.


To add a file just copy it to the dropbox folder.


To list the files that are in the peer-box type at the IDE's command line:

*files*

To view the chunks of each peer:

*chunks*

To delete file:

*delete <nome_ficheiro>*

To get and place a file in the downloads folder:

*fetch <nome_ficheiro>*





