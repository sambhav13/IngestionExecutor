package com.app.ingestion.validator.design.impl;

import com.app.ingestion.validator.design.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class RegexCriterion implements Criterion<String> {
    private static Logger LOG = LoggerFactory.getLogger(RegexCriterion.class);

    private Set<String> allowedPatterns = new HashSet<>();
    private Set<String> forbiddenPatterns = new HashSet<>();
    private String positiveRegex = ".*";
    private String negativeRegex = "^$";

    public RegexCriterion(){

    }

    public Set<String> getAllowed(){ return allowedPatterns;}

    public Set<String> getForbidden(){
        return forbiddenPatterns;
    }
    @Override
    public boolean addPositiveCriteria(String regex) {
        boolean res = allowedPatterns.add(regex);
        if(res){
            LOG.debug("single positive criteria added");
            updatePositiveCriteria();
        }
        return res;
    }

    @Override
    public boolean addPositiveCriteria(List<String> regexS) {
        boolean res = allowedPatterns.addAll(regexS);
        if(res){
            LOG.debug("List of  positive criteria added");
            updatePositiveCriteria();

        }
        return res;
    }

    @Override
    public boolean addNegativeCriteria(String regex) {
        boolean res = forbiddenPatterns.add(regex);
        if(res){
            LOG.debug("Single negative criteria added");
            updateNegativeCriteria();

        }
        return res;
    }

    @Override
    public boolean addNegativeCriteria(List<String> regexS) {
        boolean res = forbiddenPatterns.addAll(regexS);
        if(res){
            LOG.debug("List of negative criteria added");
            updateNegativeCriteria();

        }
        return res;
    }

    @Override
    public boolean removePositiveCriteria(String regex) {
        boolean res = allowedPatterns.remove(regex);
        if(res){
            LOG.debug("single positive criteria removed");
            updatePositiveCriteria();
            LOG.debug("Regex updated");
        }
        return res;
    }

    @Override
    public boolean removePositiveCriteria(List<String> regexS) {
        boolean res = allowedPatterns.removeAll(regexS);
        if(res){
            LOG.debug("List of posituve criteria removed");
            updatePositiveCriteria();
            LOG.debug("Regex updated");
        }
        return res;
    }

    @Override
    public boolean removeNegativeCriteria(String regex) {
        boolean res = forbiddenPatterns.remove(regex);
        if(res){
            LOG.debug("Single negative criteria removed");
            updateNegativeCriteria();
            LOG.debug("Regex updated");
        }
        return res;
    }

    @Override
    public boolean removeNegativeCriteria(List<String> regexS) {

        boolean res = forbiddenPatterns.removeAll(regexS);
        if(res){
            LOG.debug("List of negative criteria removed");
            updateNegativeCriteria();
            LOG.debug("Regex updated");
        }
        return res;
    }

    @Override
    public String producePositiveCriteria() {
        return positiveRegex;
    }

    @Override
    public String produceNegativeCriteria() {
        return negativeRegex;
    }

    private void updatePositiveCriteria(){
        if(forbiddenPatterns.size() == 0){
            negativeRegex = "^$";
            LOG.info(("Criterion has not negative criteria"));
        } else{
            negativeRegex =  produce(forbiddenPatterns);
        }
    }

    private void updateNegativeCriteria(){
        if(forbiddenPatterns.size() == 0) {
            negativeRegex = "^$";
            LOG.info("Criterion has not negative criteria");
        } else {
            negativeRegex =  produce(forbiddenPatterns);
        }
    }

    public String produce(Set<String> patterns) {
        LOG.debug("Constructing new regex from patterns provided");
        Iterator<String> itr = patterns.iterator();
        if( patterns.size() == 1){
            return itr.next();
        }

        String regex = "(";
        String end = ")";
        String OR = "|";
        while(itr.hasNext()) {
            regex += OR + "(" + itr.next() + ")";
        }
        regex = regex.replaceFirst("\\|","");
        regex +=end;
        return regex;
    }

}
