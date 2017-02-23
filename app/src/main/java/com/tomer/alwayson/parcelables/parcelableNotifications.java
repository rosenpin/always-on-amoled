package com.tomer.alwayson.parcelables;

import com.tomer.alwayson.services.NotificationListener;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class parcelableNotifications extends ConcurrentHashMap<String, NotificationListener.NotificationHolder> implements Serializable {

}
