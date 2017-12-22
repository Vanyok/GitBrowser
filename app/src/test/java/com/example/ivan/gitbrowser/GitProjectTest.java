package com.example.ivan.gitbrowser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ivan on 21.12.17.
 *
 */
public class GitProjectTest {
    GitProject assertProject;
    GitProject testProject;
    String avatar ="http://avatar.jpg";
    String name = "Name";
    String description = "Description";
    Integer num = 7;

    @Before
    public void setUp() throws Exception {
        assertProject = new GitProject(null,avatar,name,description,num,null);
        testProject = new GitProject();
    }
    @Test
    public void setAvatar() throws Exception {
        assertProject = new GitProject(null,avatar,null,null,null,null);
        testProject.setAvatar(avatar);
        assertEquals(assertProject,testProject);
    }

    @Test
    public void getAvatar() throws Exception {
        String actAva = assertProject.getAvatar();
        assertEquals(avatar,actAva);
    }



    @Test
    public void getRepositoryName() throws Exception {
        String actRepositoryName = assertProject.getRepositoryName();
        assertEquals(actRepositoryName,name);
    }

    @Test
    public void setRepositoryName() throws Exception {
        assertProject = new GitProject(null,null,name,null,null,null);
        testProject.setRepositoryName(name);
        assertTrue(testProject.equals(assertProject));

    }

    @Test
    public void getDescription() throws Exception {
        String actDescr = assertProject.getDescription();
        assertEquals(description,actDescr);
        assertProject.setDescription("null");
        actDescr = assertProject.getDescription();
        assertEquals("",actDescr);
    }

    @Test
    public void setDescription() throws Exception {
        assertProject = new GitProject(null,null,null,description,null,null);
        testProject.setDescription(description);
        assertEquals(assertProject,testProject);

    }

    @Test
    public void getNumOfForks() throws Exception {
        Integer actNumOfForks = assertProject.getNumOfForks();
        assertEquals(num,actNumOfForks);
    }

    @Test
    public void setNumOfForks() throws Exception {
        assertProject = new GitProject(null,null,null,null,num,null);
        testProject.setNumOfForks(num);
        assertEquals(assertProject,testProject);

    }

}