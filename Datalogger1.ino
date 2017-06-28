//#This program is a part of Geiger Muller Pulse Counter working together with java app.
//# by K.D.

unsigned long tslr = 0;                   // time since last reset
int interval = 1000;                      // interval of x time
volatile int count = 0;                  // parameter for number of counts
int blinkCount = 0;
int state = LOW;                 // parameter for LED

void count_inc()
{
  ++count;                                //increase count by 1
}

void print_count()
{
  Serial.println(count);                    //  Printing the count that came from last time frame
  count = 0;                             //  Zeroing the count for next loop
}
void flash_led()
{
  state = !state;                         //change led state
  digitalWrite(13, state);
}

void setup()
{
  pinMode(2, INPUT);                      //  attach counting pin
  pinMode(13, OUTPUT);                     //  attach onboard LED
  digitalWrite(13, state);
  Serial.begin(9600);                     //  connecting UART
  delay(1000);                            //  delay to remove random voltage spikes
}

void loop()
{
  tslr = millis();//  obtaining reference
  attachInterrupt(0, count_inc, FALLING); 
  while ((millis() - tslr) < interval)   //  counting time frame
  {
    if (blinkCount <= count)
    {
      flash_led();      //change the state of onboard LED to signal count change
      ++blinkCount;
    }
  }
  detachInterrupt(0);   
  print_count();
}
