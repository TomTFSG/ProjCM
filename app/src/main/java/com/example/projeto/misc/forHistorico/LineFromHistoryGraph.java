package com.example.projeto.misc.forHistorico;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;

import java.util.List;

public class LineFromHistoryGraph {
    Mapping mapping;
    String strokeColor, name;
    Set colecao;

    public LineFromHistoryGraph(
            String strokeColor,
            String name,
            String mappingString,
            List<DataEntry> data
    ){
        colecao = Set.instantiate();
        colecao.data(data);
        mapping = colecao.mapAs(mappingString);

        this.strokeColor = strokeColor;
        this.name = name;
    }



    public void doLineOnCartesian(Cartesian cartesian){
        Line line = cartesian.line(mapping);

        line.name(name);
        line.stroke(strokeColor);
        line.hovered().markers().enabled(true);
        line.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        line.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
    }
}
