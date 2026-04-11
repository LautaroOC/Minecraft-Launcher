package dev.integrity;

import java.util.ArrayList;

public class IssueReport {
    private ArrayList<Issue> issues;

    public IssueReport() {
        issues = new ArrayList<>();
    }

    public void addIssue(Issue issue) {
        issues.add(issue);
    }
}
