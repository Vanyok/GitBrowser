package com.example.ivan.gitbrowser;

/**
 * Created by ivan on 20.12.17.
 * model which present git project data set
 */

public class GitProject {
    private Integer id;
    private String avatar;
    private String repositoryName;
    private String description;
    private Integer numOfForks;
    private String linkToSubscr;

    GitProject() {

    }

    GitProject(Integer id,
               String avatar,
               String repositoryName,
               String description,
               Integer numOfForks,
               String linkToSubscr) {
        this.avatar = avatar;
        this.repositoryName = repositoryName;
        this.description = description;
        this.numOfForks = numOfForks;
        this.id = id;
        this.linkToSubscr = linkToSubscr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    String getAvatar() {
        return avatar;
    }

    void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    String getRepositoryName() {
        return repositoryName;
    }

    void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }


    String getDescription() {
        //replace string null to empty string
        if (!description.equals("null")) {
            return description;
        } else {
            return "";
        }

    }

    public void setDescription(String description) {
        this.description = description;
    }

    Integer getNumOfForks() {
        return numOfForks;
    }

    void setNumOfForks(Integer numOfForks) {
        this.numOfForks = numOfForks;
    }


    String getLinkToSubscr() {
        return linkToSubscr;
    }

    public void setLinkToSubscr(String linkToSubscr) {
        this.linkToSubscr = linkToSubscr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitProject project = (GitProject) o;

        if (id != null ? !id.equals(project.id) : project.id != null) return false;
        if (avatar != null ? !avatar.equals(project.avatar) : project.avatar != null) return false;
        if (repositoryName != null ? !repositoryName.equals(project.repositoryName) : project.repositoryName != null)
            return false;
        if (description != null ? !description.equals(project.description) : project.description != null)
            return false;
        if (numOfForks != null ? !numOfForks.equals(project.numOfForks) : project.numOfForks != null)
            return false;
        return linkToSubscr != null ? linkToSubscr.equals(project.linkToSubscr) : project.linkToSubscr == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (repositoryName != null ? repositoryName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (numOfForks != null ? numOfForks.hashCode() : 0);
        result = 31 * result + (linkToSubscr != null ? linkToSubscr.hashCode() : 0);
        return result;
    }
}
