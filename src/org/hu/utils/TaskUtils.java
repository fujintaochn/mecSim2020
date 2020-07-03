package org.hu.utils;

public class TaskUtils {
    private static int TASK_ID = 1;

    public static String generateTaskId(){
        return Integer.toString(TASK_ID++);
    }
}
