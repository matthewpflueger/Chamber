require(
    [
        'requireLib',
        'jquery',
        'components/widget/gallery'
    ],
    function(requireLib, $, Gallery){

        $(document).ready(function(){
            var properties = Echoed;
            var gallery = new Gallery({ properties: properties})
        });
  }
);