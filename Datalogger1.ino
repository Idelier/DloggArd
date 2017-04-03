int receiveddata = 0;
unsigned long tslr = 0; // time since last reset
int interval = 1000; //interval of 1 s
long count = 0; // parameter for number of counts

void count_inc()
{
  count++;
}

void count_start()
{
  attachInterrupt(0, counting_stop, FALLING); // ready to stop
  attachInterrupt(1, count_inc, RISING); // ready to count pulses
}

void count_stop()
{
  detachInterrupt(1); // stop counting pulses
  attachInterrupt(0, counting_start, RISING); // ready to start
}



void setup() 
{
  PinMode(3, INPUT);
  digitalWrite(3, HIGH);
  Serial.begin(9600);
  delay(1000);  // delay to remove random voltage spike
}

void loop() 
{
  
  tslr = millis(); // obtaining reference
   while((millis() - tslr) < interval)
   {
    count_start();     
   }
   Serial.println(count); //Printing the count that came from last 1s
   count = 0; // Zeroing the count for next loop
}
