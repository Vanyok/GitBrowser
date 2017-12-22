package com.example.ivan.gitbrowser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by ivan on 21.12.17.
 *
 */
@RunWith(AndroidJUnit4.class)
public class JsonToProjectListTest {
    ArrayList<GitProject> expectedList;
    String jResponce = DataResuorce.getResponceProjects();
    String sResponce = DataResuorce.getResponceSubscribers();


    @Before
    public void setUp() throws Exception {
        expectedList = new ArrayList<>();
        GitProject project = new GitProject(76954504,"https://avatars2.githubusercontent.com/u/5383506?v=4",
               "react-tetris",
                "Use React, Redux, Immutable to code Tetris. \uD83C\uDFAE",
                485,"https://api.github.com/repos/chvin/react-tetris/subscribers"
                );
        expectedList.add(project);
        project = new GitProject(12014401,"https://avatars2.githubusercontent.com/u/5201002?v=4",
                "Tetris",
                "Tetris Project",
                55,"https://api.github.com/repos/PSNB92/Tetris/subscribers");
        expectedList.add(project);
    }


    @Test
    public void convert() throws Exception {
        ArrayList<GitProject> actual = JsonToProjectList.convertToProjects(jResponce);
        assertEquals(expectedList,actual);
    }

    @Test
    public void convertToSubscribers() throws Exception {
        ArrayList<Subscriber> actual = JsonToProjectList.convertToSubscribers(sResponce);
        int actSize = actual.size();
        assertEquals(20,actSize);
    }

}