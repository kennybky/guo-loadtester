(function() {
    angular
        .module('app')
        .factory('tableItems', tableItems);

    function tableItems() {
        var service = {
            addSelectedAttr: addSelectedAttr,
            getSelectedItems: getSelectedItems,
            getSelectedItem: getSelectedItem,
            selectAll: selectAll
        }

        return service;

        function addSelectedAttr(item) {
            item["selected"] = false;
            return item;
        }

        function getSelectedItems(items) {
            if (items !== undefined) {
                var selectedItems = items.filter(function (item) {
                    return item.selected === true;
                });
                return selectedItems;
            }
        }

        function getSelectedItem(items) {
            return items.find(function (item) {
                return item.selected;
            });
        }

        function selectAll(items, selectAll) {
            items.forEach(function (item) {
                if (selectAll) {
                    item.selected = true;
                } else {
                    item.selected = false;
                }
            });
        }
    }
})();