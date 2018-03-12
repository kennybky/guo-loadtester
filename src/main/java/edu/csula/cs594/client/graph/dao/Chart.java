package edu.csula.cs594.client.graph.dao;

public class Chart {

    private String caption;
    private String subcaption;
    private String xaxisname;
    private String yaxisname;
    private int xaxisminvalue;
    private int xaxismaxvalue;
    private String showAxisLines;
    private String ynumberprefix;
    private String xnumbersuffix;
    private String theme;    
    private String subCaption;
    private String xAxisName;
    private String yAxisName;
    private String divlineAlpha;
    private String numberPrefix;
    private String paletteColors;
    private String bgColor;
    private String showBorder; // 0,
    private String showCanvasBorder; // 0,
    private String plotBorderAlpha; // 10,
    private String usePlotGradientColor; // 0,
    private String legendBorderAlpha; // 0,
    private String legendShadow; // 0,
    private String plotFillAlpha; // 60,
    private String showXAxisLine; // 1,
    private String axisLineAlpha; // 25,
    private String showValues; // 0,
    private String captionFontSize; // 16,
    private String subcaptionFontSize; // 16,
    private String subcaptionFontBold; // 0,
    private String divlineColor; // #999999,
    private String divLineIsDashed; // 1,
    private String divLineDashLen; // 1,
    private String divLineGapLen; // 1,
    private String showAlternateHGridColor; // 0,
    private String toolTipColor; // #ffffff,
    private String toolTipBorderThickness; // 0,
    private String toolTipBgColor; // #000000,
    private String toolTipBgAlpha; // 80,
    private String toolTipBorderRadius; // 2,
    private String toolTipPadding; // 5
    private String showShadow;
    private String lineThickness;
    private String flatScrollBars;
    private String scrollHeight;
    private String numVisiblePlot;
    private String showHoverEffect;
    private String divlineThickness;

    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the subcaption
     */
    public String getSubcaption() {
        return subcaption;
    }

    /**
     * @param subcaption the subcaption to set
     */
    public void setSubcaption(String subcaption) {
        this.subcaption = subcaption;
    }

    /**
     * @return the xaxisname
     */
    public String getXaxisname() {
        return xaxisname;
    }

    /**
     * @param xaxisname the xaxisname to set
     */
    public void setXaxisname(String xaxisname) {
        this.xaxisname = xaxisname;
    }

    /**
     * @return the yaxisname
     */
    public String getYaxisname() {
        return yaxisname;
    }

    /**
     * @param yaxisname the yaxisname to set
     */
    public void setYaxisname(String yaxisname) {
        this.yaxisname = yaxisname;
    }

    /**
     * @return the xaxisminvalue
     */
    public int getXaxisminvalue() {
        return xaxisminvalue;
    }

    /**
     * @param xaxisminvalue the xaxisminvalue to set
     */
    public void setXaxisminvalue(int xaxisminvalue) {
        this.xaxisminvalue = xaxisminvalue;
    }

    /**
     * @return the xaxismaxvalue
     */
    public int getXaxismaxvalue() {
        return xaxismaxvalue;
    }

    /**
     * @param xaxismaxvalue the xaxismaxvalue to set
     */
    public void setXaxismaxvalue(int xaxismaxvalue) {
        this.xaxismaxvalue = xaxismaxvalue;
    }

    /**
     * @return the ynumberprefix
     */
    public String getYnumberprefix() {
        return ynumberprefix;
    }

    /**
     * @param ynumberprefix the ynumberprefix to set
     */
    public void setYnumberprefix(String ynumberprefix) {
        this.ynumberprefix = ynumberprefix;
    }

    /**
     * @return the xnumbersuffix
     */
    public String getXnumbersuffix() {
        return xnumbersuffix;
    }

    /**
     * @param xnumbersuffix the xnumbersuffix to set
     */
    public void setXnumbersuffix(String xnumbersuffix) {
        this.xnumbersuffix = xnumbersuffix;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the subCaption
     */
    public String getSubCaption() {
        return subCaption;
    }

    /**
     * @param subCaption the subCaption to set
     */
    public void setSubCaption(String subCaption) {
        this.subCaption = subCaption;
    }

    /**
     * @return the xAxisName
     */
    public String getxAxisName() {
        return xAxisName;
    }

    /**
     * @param xAxisName the xAxisName to set
     */
    public void setxAxisName(String xAxisName) {
        this.xAxisName = xAxisName;
    }

    /**
     * @return the yAxisName
     */
    public String getyAxisName() {
        return yAxisName;
    }

    /**
     * @param yAxisName the yAxisName to set
     */
    public void setyAxisName(String yAxisName) {
        this.yAxisName = yAxisName;
    }

    /**
     * @return the numberPrefix
     */
    public String getNumberPrefix() {
        return numberPrefix;
    }

    /**
     * @param numberPrefix the numberPrefix to set
     */
    public void setNumberPrefix(String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    /**
     * @return the paletteColors
     */
    public String getPaletteColors() {
        return paletteColors;
    }

    /**
     * @param paletteColors the paletteColors to set
     */
    public void setPaletteColors(String paletteColors) {
        this.paletteColors = paletteColors;
    }

    /**
     * @return the bgColor
     */
    public String getBgColor() {
        return bgColor;
    }

    /**
     * @param bgColor the bgColor to set
     */
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * @return the showBorder
     */
    public String getShowBorder() {
        return showBorder;
    }

    /**
     * @param showBorder the showBorder to set
     */
    public void setShowBorder(String showBorder) {
        this.showBorder = showBorder;
    }

    /**
     * @return the showCanvasBorder
     */
    public String getShowCanvasBorder() {
        return showCanvasBorder;
    }

    /**
     * @param showCanvasBorder the showCanvasBorder to set
     */
    public void setShowCanvasBorder(String showCanvasBorder) {
        this.showCanvasBorder = showCanvasBorder;
    }

    /**
     * @return the plotBorderAlpha
     */
    public String getPlotBorderAlpha() {
        return plotBorderAlpha;
    }

    /**
     * @param plotBorderAlpha the plotBorderAlpha to set
     */
    public void setPlotBorderAlpha(String plotBorderAlpha) {
        this.plotBorderAlpha = plotBorderAlpha;
    }

    /**
     * @return the usePlotGradientColor
     */
    public String getUsePlotGradientColor() {
        return usePlotGradientColor;
    }

    /**
     * @param usePlotGradientColor the usePlotGradientColor to set
     */
    public void setUsePlotGradientColor(String usePlotGradientColor) {
        this.usePlotGradientColor = usePlotGradientColor;
    }

    /**
     * @return the legendBorderAlpha
     */
    public String getLegendBorderAlpha() {
        return legendBorderAlpha;
    }

    /**
     * @param legendBorderAlpha the legendBorderAlpha to set
     */
    public void setLegendBorderAlpha(String legendBorderAlpha) {
        this.legendBorderAlpha = legendBorderAlpha;
    }

    /**
     * @return the legendShadow
     */
    public String getLegendShadow() {
        return legendShadow;
    }

    /**
     * @param legendShadow the legendShadow to set
     */
    public void setLegendShadow(String legendShadow) {
        this.legendShadow = legendShadow;
    }

    /**
     * @return the plotFillAlpha
     */
    public String getPlotFillAlpha() {
        return plotFillAlpha;
    }

    /**
     * @param plotFillAlpha the plotFillAlpha to set
     */
    public void setPlotFillAlpha(String plotFillAlpha) {
        this.plotFillAlpha = plotFillAlpha;
    }

    /**
     * @return the showXAxisLine
     */
    public String getShowXAxisLine() {
        return showXAxisLine;
    }

    /**
     * @param showXAxisLine the showXAxisLine to set
     */
    public void setShowXAxisLine(String showXAxisLine) {
        this.showXAxisLine = showXAxisLine;
    }

    /**
     * @return the axisLineAlpha
     */
    public String getAxisLineAlpha() {
        return axisLineAlpha;
    }

    /**
     * @param axisLineAlpha the axisLineAlpha to set
     */
    public void setAxisLineAlpha(String axisLineAlpha) {
        this.axisLineAlpha = axisLineAlpha;
    }

    /**
     * @return the showValues
     */
    public String getShowValues() {
        return showValues;
    }

    /**
     * @param showValues the showValues to set
     */
    public void setShowValues(String showValues) {
        this.showValues = showValues;
    }

    /**
     * @return the captionFontSize
     */
    public String getCaptionFontSize() {
        return captionFontSize;
    }

    /**
     * @param captionFontSize the captionFontSize to set
     */
    public void setCaptionFontSize(String captionFontSize) {
        this.captionFontSize = captionFontSize;
    }

    /**
     * @return the subcaptionFontSize
     */
    public String getSubcaptionFontSize() {
        return subcaptionFontSize;
    }

    /**
     * @param subcaptionFontSize the subcaptionFontSize to set
     */
    public void setSubcaptionFontSize(String subcaptionFontSize) {
        this.subcaptionFontSize = subcaptionFontSize;
    }

    /**
     * @return the subcaptionFontBold
     */
    public String getSubcaptionFontBold() {
        return subcaptionFontBold;
    }

    /**
     * @param subcaptionFontBold the subcaptionFontBold to set
     */
    public void setSubcaptionFontBold(String subcaptionFontBold) {
        this.subcaptionFontBold = subcaptionFontBold;
    }

    /**
     * @return the divlineColor
     */
    public String getDivlineColor() {
        return divlineColor;
    }

    /**
     * @param divlineColor the divlineColor to set
     */
    public void setDivlineColor(String divlineColor) {
        this.divlineColor = divlineColor;
    }

    /**
     * @return the divLineIsDashed
     */
    public String getDivLineIsDashed() {
        return divLineIsDashed;
    }

    /**
     * @param divLineIsDashed the divLineIsDashed to set
     */
    public void setDivLineIsDashed(String divLineIsDashed) {
        this.divLineIsDashed = divLineIsDashed;
    }

    /**
     * @return the divLineDashLen
     */
    public String getDivLineDashLen() {
        return divLineDashLen;
    }

    /**
     * @param divLineDashLen the divLineDashLen to set
     */
    public void setDivLineDashLen(String divLineDashLen) {
        this.divLineDashLen = divLineDashLen;
    }

    /**
     * @return the divLineGapLen
     */
    public String getDivLineGapLen() {
        return divLineGapLen;
    }

    /**
     * @param divLineGapLen the divLineGapLen to set
     */
    public void setDivLineGapLen(String divLineGapLen) {
        this.divLineGapLen = divLineGapLen;
    }

    /**
     * @return the showAlternateHGridColor
     */
    public String getShowAlternateHGridColor() {
        return showAlternateHGridColor;
    }

    /**
     * @param showAlternateHGridColor the showAlternateHGridColor to set
     */
    public void setShowAlternateHGridColor(String showAlternateHGridColor) {
        this.showAlternateHGridColor = showAlternateHGridColor;
    }

    /**
     * @return the toolTipColor
     */
    public String getToolTipColor() {
        return toolTipColor;
    }

    /**
     * @param toolTipColor the toolTipColor to set
     */
    public void setToolTipColor(String toolTipColor) {
        this.toolTipColor = toolTipColor;
    }

    /**
     * @return the toolTipBorderThickness
     */
    public String getToolTipBorderThickness() {
        return toolTipBorderThickness;
    }

    /**
     * @param toolTipBorderThickness the toolTipBorderThickness to set
     */
    public void setToolTipBorderThickness(String toolTipBorderThickness) {
        this.toolTipBorderThickness = toolTipBorderThickness;
    }

    /**
     * @return the toolTipBgColor
     */
    public String getToolTipBgColor() {
        return toolTipBgColor;
    }

    /**
     * @param toolTipBgColor the toolTipBgColor to set
     */
    public void setToolTipBgColor(String toolTipBgColor) {
        this.toolTipBgColor = toolTipBgColor;
    }

    /**
     * @return the toolTipBgAlpha
     */
    public String getToolTipBgAlpha() {
        return toolTipBgAlpha;
    }

    /**
     * @param toolTipBgAlpha the toolTipBgAlpha to set
     */
    public void setToolTipBgAlpha(String toolTipBgAlpha) {
        this.toolTipBgAlpha = toolTipBgAlpha;
    }

    /**
     * @return the toolTipBorderRadius
     */
    public String getToolTipBorderRadius() {
        return toolTipBorderRadius;
    }

    /**
     * @param toolTipBorderRadius the toolTipBorderRadius to set
     */
    public void setToolTipBorderRadius(String toolTipBorderRadius) {
        this.toolTipBorderRadius = toolTipBorderRadius;
    }

    /**
     * @return the toolTipPadding
     */
    public String getToolTipPadding() {
        return toolTipPadding;
    }

    /**
     * @param toolTipPadding the toolTipPadding to set
     */
    public void setToolTipPadding(String toolTipPadding) {
        this.toolTipPadding = toolTipPadding;
    }

    public String getShowShadow() {
        return showShadow;
    }

    public void setShowShadow(String showShadow) {
        this.showShadow = showShadow;
    }

    public String getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(String lineThickness) {
        this.lineThickness = lineThickness;
    }

    public String getFlatScrollBars() {
        return flatScrollBars;
    }

    public void setFlatScrollBars(String flatScrollBars) {
        this.flatScrollBars = flatScrollBars;
    }

    public String getScrollHeight() {
        return scrollHeight;
    }

    public void setScrollHeight(String scrollHeight) {
        this.scrollHeight = scrollHeight;
    }

    public String getNumVisiblePlot() {
        return numVisiblePlot;
    }

    public void setNumVisiblePlot(String numVisiblePlot) {
        this.numVisiblePlot = numVisiblePlot;
    }

    public String getShowHoverEffect() {
        return showHoverEffect;
    }

    public void setShowHoverEffect(String showHoverEffect) {
        this.showHoverEffect = showHoverEffect;
    }

    public String getShowAxisLines() {
        return showAxisLines;
    }

    public void setShowAxisLines(String showAxisLines) {
        this.showAxisLines = showAxisLines;
    }

    public String getDivlineAlpha() {
        return divlineAlpha;
    }

    public void setDivlineAlpha(String divlineAlpha) {
        this.divlineAlpha = divlineAlpha;
    }

    public String getDivlineThickness() {
        return divlineThickness;
    }

    public void setDivlineThickness(String divlineThickness) {
        this.divlineThickness = divlineThickness;
    }
}
