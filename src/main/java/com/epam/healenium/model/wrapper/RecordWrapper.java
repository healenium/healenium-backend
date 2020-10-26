package com.epam.healenium.model.wrapper;

import com.epam.healenium.model.domain.Report;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RecordWrapper {

    private Set<Report.Record> records = new HashSet<>();

}
