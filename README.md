# Data Analysis

Flat files reader built in Java.





## Quick Start

1 - Create a new MySQL database called etracker and run the scripts located at: etracker-server/scripts/database

2 - Build the Etracker backend server: mvn clean install

3 - Start the backend server: java -jar etracker-server/target/etracker-server-1.0.jar

    The backend server will run in the port 8090
    To perform calls to the REST API, use these client credentials:
    User: etracker-client
    Password: oRUYY81378
    Grant type: password






## Requirements

The system must be able to import lots of flat files, read and analyse
the data, and output a report.

### Flat file layout (INPUT)

There are 3 kinds of data inside those files .For each kind of data there
is a different layout.

#### Salesman data
Salesman data has the format id 001 and the line will have the following
format.

    001çCPFçNameçSalary

#### Customer data
Customer data has the format id 002 and the line will have the following
format.

    002çCNPJçNameçBusinessArea

#### Sales data
Sales data has the format id 003. Inside the sales row, there is the list
of items, which is wrapped by square brackets [].
The line will have the following format.

    003çSaleIDç[ItemID-ItemQuantity-ItemPrice]çSalesmanname

#### Sample file data (INPUT)
The following is a sample of the data that the application should be
able to read. Note that this is a sample, real data could be 100% different.

    001ç1234567891234çDiegoç50000
    001ç3245678865434çRenatoç40000.99
    002ç2345675434544345çJosedaSilvaçRural
    002ç2345675433444345çEduardoPereiraçRural
    003ç10ç[1-10-100,2-30-2.50,3-40-3.10]çDiego
    003ç08ç[1-34-10,2-33-1.50,3-40-0.10]çRenato

### Data analysis

The system must read data from the default directory,
located at %HOMEPATH%/data/in. The system must only read .dat files.

After processing all files inside the input default directory,
the system must create a flat file inside the default output directory,
located at %HOMEPATH%/data/out.
The filename must follow this pattern, {flat_file_name}.done.dat.

The output file contents should summarize the following data:

- Amount of clients in the input file
- Amount of salesman in the input file
- ID of the most expensive sale
- Worst salesman ever

This application should be running all the time, without any breaks.
Everytime new files become available, everything should be executed.


### Flat file layout (OUTPUT)

There are 4 kinds of data inside those files. For each kind of data there
is a different layout. There will be only one line of each kind of data.

#### Amount of Clients
The amount of clients has the format id 001 and the line will have
the following format.

    001çAmountClientsç2

#### Amount of Salesman
The amount of salesman has the format id 002 and the line will have
the following format.

    002çAmountSalesmanç2

#### ID of the most expensive sale
The ID of the most expensive sale has the format id 003 and the line
will have the following format.

    003çMostExpensiveSaleç10

#### Worst salesman ever
The Worst salesman ever has the format id 004 and the line will have
the following format.

    004çWorstSalesmançRenato


#### Sample file data (OUTPUT)
The following is a sample of the data that the application should be
able to write.

    001çAmountClientsç2
    002çAmountSalesmanç2
    003çMostExpensiveSaleç10
    004çWorstSalesmançRenato
