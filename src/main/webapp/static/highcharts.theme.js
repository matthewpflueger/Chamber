Highcharts.theme = {
        yAxis: {
            min: 0,
            title: {
                style:{
                    color: '#000000'
                }
            },
            labels:{
                enabled: true,
                style:{
                    color: '#000000',
                    fontFamily:'Tahoma,Arial',
                    fontSize: '14px'
                }
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    fontFamily:'BebasNeue',
                    fontSize: '24px',
                    color: '#FF7733'
                }
            },
            gridLineWidth:1,
            lineWidth: 1,
            lineColor: '#000'

        },
        xAxis: {
            lineColor: '#000',
                labels:{
                    style:{
                        color: '#000000',
                        fontFamily:'Tahoma,Arial',
                        fontSize: '14px'
                    }
                }
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: false,
                    color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'black',
                    style:{
                        fontFamily: 'BebasNeue',
                        fontWeight: 'bold',
                        fontSize: '24px'
                    }
                }
            },
            area:{
                stacking: 'normal'
            },
            series:{
                pointWidth: 70,
                pointPadding: 0,
                groupPadding:0,
                borderWidth: 1,
                borderColor: "#666",
                shadow: false
            }
        },
        legend: {
            align: 'center',
            verticalAlign: 'bottom',
            y: 15,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
            borderColor: '#CCC',
            borderWidth: 0,
            shadow: false
        }
    };

    var highchartsOptions  = Highcharts.setOptions(Highcharts.theme);

