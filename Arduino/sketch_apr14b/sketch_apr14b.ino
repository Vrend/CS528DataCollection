#include <DFRobot_LIS2DH12.h>

#include <Wire.h>

#define heartratePin A2
DFRobot_LIS2DH12 accel;

int threshhold = 1020;
int bpm = 0;
int timer = 0;
int magnitude = 0;
int mag_min = 999999;
int accel_counter = 0;

void setup() {
  Wire.begin();
  Serial.begin(115200);
  while(accel.init(LIS2DH12_RANGE_2GA) == -1) {
    Serial.println("Accelerometer not detected. Check address jumper and wiring.");
    delay(1000);
  }
}

void loop() {

//  if(accel_counter == 5) {
//    magnitude /= 5;
//    print_accel();
//    magnitude = 0;
//    mag_min = 999999;
//    accel_counter = 0;
//  }

//  if(timer >= 10000) {
//    bpm = bpm*6;
//    Serial.print("BPM: ");
//    Serial.println(bpm);
//    bpm = 0;
//    timer = 0;
////    magnitude /= accel_counter;
////    print_accel();
////    magnitude = 0;
//    accel_counter = 0;
//  }
//  acceleration();
  int heartValue = analogRead(heartratePin);
  Serial.println(heartValue);
  delay(100);
//  if(heartValue > threshhold) {
//    bpm++;
//    delay(250);
//    timer += 250;
//  }
//  else {
//    timer += 50;
//    delay(50);
//  }
}

void acceleration(void) {
  int16_t x, y, z;
  accel.readXYZ(x, y, z);
  accel.mgScale(x, y, z);
  int32_t x2, y2, z2;
  x2 = int32_t(x);
  y2 = int32_t(y);
  z2 = int32_t(z);
  int point_magnitude = sqrt(sq(x2)+sq(y2)+sq(z2));
  if(point_magnitude < mag_min) {
     mag_min = point_magnitude;
  }
  magnitude += point_magnitude;
  Serial.print("x: ");
  Serial.print(x);
  Serial.print(" y: ");
  Serial.print(y);
  Serial.print(" z: ");
  Serial.println(z);
  //print_accel();
  accel_counter++;
  delay(100);
}

void print_accel(void) {
  Serial.print("Acceleration Magnitude: "); //print acceleration
  Serial.println(magnitude-mag_min);
}
