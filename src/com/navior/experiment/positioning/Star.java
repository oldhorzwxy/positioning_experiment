/**
 * ==============================BEGIN_COPYRIGHT===============================
 * ===================NAVIOR CO.,LTD. PROPRIETARY INFORMATION==================
 * This software is supplied under the terms of a license agreement or
 * nondisclosure agreement with NAVIOR CO.,LTD. and may not be copied or
 * disclosed except in accordance with the terms of that agreement.
 * ==========Copyright (c) 2003 NAVIOR CO.,LTD. All Rights Reserved.===========
 * ===============================END_COPYRIGHT================================
 *
 * @author wangxiayang
 * @date 21/08/13
 */
package com.navior.experiment.positioning;

public class Star {
  private int x;
  private int y;
  private String name;
  private int rssi;

  public Star(String name, int x, int y) {
    setName(name);
    setX(x);
    setY(y);
    setRssi(-100);
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRssi() {
    return rssi;
  }

  public void setRssi(int rssi) {
    this.rssi = rssi;
  }
}
