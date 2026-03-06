package dev;

import java.util.ArrayList;

public class IssueReport {
    private ArrayList<String> issues;

    public IssueReport() {
        issues = new ArrayList<>();
    }

    public void addIssue(String issue) {
        issues.add(issue);
    }
}
