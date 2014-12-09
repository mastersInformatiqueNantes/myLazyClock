/*
 * myLazyClock
 *
 * Copyright (C) 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.myLazyClock.calendarApi;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.myLazyClock.calendarApi.exception.EventNotFoundException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

/**
 * Created on 28/10/14.
 *
 * @author dralagen
 */
public class CalendarGoogleStrategy implements CalendarStrategy {

    public static final int ID = 3;

    @Override
    public Integer getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Calendar Google";
    }
    
    @Override
    public CalendarEvent getFirstEvent(String url, java.util.Calendar day, Map<String, String> params) throws EventNotFoundException {
        if (day == null || params == null || params.get("tokenRequest") == null || params.get("tokenRequest").equals("")) {
            throw new EventNotFoundException();
        }

        DateTime startTime = new DateTime(day.getTime());
        java.util.Calendar endDay = (java.util.Calendar) day.clone();
        endDay.add(java.util.Calendar.DATE, 1);
        DateTime endTime = new DateTime(endDay.getTime());



        CalendarEvent returnEvent = new CalendarEvent();

        try {

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setClientSecrets(params.get("apiId"),params.get("apiSecret"))
                    .build().setRefreshToken(params.get("tokenRequest"));

            Calendar service = new Calendar.Builder(httpTransport, jsonFactory, null)
                    .setApplicationName("myLazyClock")
                    .setHttpRequestInitializer(credential).build();


            Events events = service.events().list(params.get("gCalId"))
                    .setTimeMin(startTime)
                    .setTimeMax(endTime)
                    .setOrderBy("startTime")
                    .setSingleEvents(true).execute();

            if (events.getItems().isEmpty()) {
                throw new EventNotFoundException();
            }

            Event event = events.getItems().get(0);

            returnEvent.setName(event.getSummary());
            returnEvent.setBeginDate(new Date(event.getStart().getDateTime().getValue()));
            returnEvent.setEndDate(new Date(event.getEnd().getDateTime().getValue()));
            returnEvent.setAddress(event.getLocation());

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new EventNotFoundException();
        }

    	return returnEvent;
    }
}