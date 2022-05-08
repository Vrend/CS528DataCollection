#include <DFRobot_LIS2DH12.h>
#include <Wire.h>

#define heartratePin A2


const int vibratePin = 3;

DFRobot_LIS2DH12 accel;

/*
* Stores 140 50-millisecond-apart instances of accelerometer and pulse data
* Each dimension of the accelerometer are stored separately
*/
int16_t x_vals[40];
int16_t y_vals[40];
int16_t z_vals[40];
int16_t pulse_vals[40];


/*
* State variable decides whether to collect data or not. This is decided by the 
* app or serial monitor. When false, don't collect data. When true, do collect data.
*/
int state = 0;

/*
* Keeps track of how many values have been stored
*/
uint8_t counter = 0;


/*
* Reads accelerometer values and stores in the dimensional integer in its
* respective array
*/
void acceleration(void) {
  int16_t x, y, z;
  accel.readXYZ(x, y, z);
  accel.mgScale(x, y, z);
  x_vals[counter] = x;
  y_vals[counter] = y;
  z_vals[counter] = z;
}

void setup() {
  Wire.begin();
  Serial.begin(115200);
  pinMode(vibratePin, OUTPUT);
  /*
  * Initialize every element of all 4 storage arrays to 0
  */
  for(int x = 0; x < 40; x++) {
    x_vals[x] = 0;
    y_vals[x] = 0;
    z_vals[x] = 0;
    pulse_vals[x] = 0;
  }

  /*
  * Initialize accelerometer with the most-sensitive mode +/- 2g
  */
  while(accel.init(LIS2DH12_RANGE_2GA) == -1) {
    Serial.println(F("Accelerometer not detected. Check address jumper and wiring."));
    delay(1000);
  }
}

void loop() {
  if(state == 2) { // calibrate pulse sensor
    int16_t pulse = analogRead(heartratePin);
    Serial.println(pulse);
    delay(50);
  }
  else if(state == 1) { // if set to collect data, do so
      if(counter == 40) { // Time to send the data
        for(int x = 0; x < 40; x++) { // give a quadruple value
          Serial.print("[");
          Serial.print(x_vals[x]);
          Serial.print(",");
          Serial.print(y_vals[x]);
          Serial.print(",");
          Serial.print(z_vals[x]);
          Serial.print(",");
          Serial.print(pulse_vals[x]);
          Serial.println("]");
          Serial.flush();
        }
        counter = 0; // reset the counter
      }
    acceleration(); // collect accelerometer data
    pulse_vals[counter] = analogRead(heartratePin); // collect pulse data
    counter++; // increment counter
    delay(50); // 50 millisecond delay
  }
  else { // if state is false (don't collect data), just wait 100 milliseconds before checking
    delay(100);
  }
  if(Serial.available()) { // check if state has changed
    char out = Serial.read();
    if(out == '0') {
      state = 0;
      counter = 0; // reset counter
    }
    else if(out == '1') {
      state = 1;
    }
    else if(out == '2') {
      state = 2;
      counter = 0;
    }
    else if(out == '3') {
      Serial.println("VIBRATE");
      analogWrite(vibratePin, 150);
      delay(2000);
      analogWrite(vibratePin, 0);
    }
  }
}
