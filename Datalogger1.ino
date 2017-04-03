int receiveddata = 0;
unsigned long tslr = 0; // time since last reset
int interval = 1000; //interval of 1 s
long count = 0; // parameter for number of counts

void count()
{
  count++;
}


void setup() 
{
  Serial.begin(9600);
  delay(1000);  // delay to remove random voltage spike
}

void loop() 
{
  receiveddata = analogRead(A5);
  tslr = millis(); // obtaining reference
   while((millis() - tslr) < interval)
   {
    if(receiveddata == 5 || receiveddata == -5)  //Check if received signal if yes then add count
    {
       count();
    } 
   }
   Serial.println(count); //Printing the count that came from last 1s
   count = 0; // Zeroing the count for next loop
}
