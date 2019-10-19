# file-process-concurrently-scala

Simple scala application to process a log file concurrently.

The file should have the following format:

```
{"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495212}
{"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}
{"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}
{"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495217}
{"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}
{"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}
```

Every line in the file is a JSON object containing event data:

 - **id**: the unique event identifier
 - **state**: whether the event was started or finished (can have values "STARTED" or "FINISHED" timestamp) 
 - **timestamp**: the timestamp of the event in milliseconds
 - **host**: hostname (optional)
 - **type**: type of log (optional)
 
This log file is supposed to be created by a server which logs different events to it. 
Every event has 2 entries in a log:
  - one entry when the event was started
  - another when the event was finished. 

The entries in a log file have no specific order (it can occur that a specific event is logged before the event starts)

In the example above, the event scsmbstgrb duration is 1401377495216 - 1491377495213 = 3ms The longest event is scsmbstgrc (1491377495218 - 1491377495210 = 8ms)

The application: 

- Takes the input file path as input argument
- Flags any long events that take longer than 4ms with a column in the database called "alert" Write the found event details to file-based H2 in the working folder
- Creates a new table if necessary and enter the following values: 
  * Event id
  * Event duration
  * Type and Host if applicable "alert" true is applicable



