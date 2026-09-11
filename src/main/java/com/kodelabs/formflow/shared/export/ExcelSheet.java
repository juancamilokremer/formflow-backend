package com.kodelabs.formflow.shared.export;

import java.util.List;

/** One sheet's worth of data for {@link ExcelRowWriter}: row 0 is the header. */
public record ExcelSheet(String name, List<List<String>> rows) {}
