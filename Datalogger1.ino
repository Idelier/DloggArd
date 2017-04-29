#This program is a part of Geiger Muller Pulse Counter working together with java app.
# by K.D.

unsigned long tslr = 0;                    // time since last reset
int interval = 1000;                       // interval of 1 s
long count = 0;                            // parameter for number of counts
violatile int state = LOW;                 // parameter for LED


void count_inc()
{
  ++count;
}

void print_count()
{
  Serial.println(count);                  //  Printing the count that came from last 1s
   count = 0;                             //  Zeroing the count for next loop
}
void flash_led()
{
  state = !state;
}

void count_start()
{
  attachInterrupt(0, count_inc, FALLING); //  add a count
  attachInterrupt(1, flash_led, FALLING); //  flash the led
  detachInterrupt(0);                     //  stop counting pulse
  detachInterrupt(1);                     //  stop flash
}

void setup() 
{
  pinMode(2, INPUT);                      //  attach counting pin
  pinMode(3, INPUT);                      //  attach blinking pin
  Serial.begin(9600);                     //  connecting UART
  delay(1000);                            //  delay to remove random voltage spikes
}

void loop() 
{
  
  tslr = millis();                        //  obtaining reference
   while((millis() - tslr) < interval)    //  counting time frame
   {
    count_start();     
   }
   print_count();                         
}
