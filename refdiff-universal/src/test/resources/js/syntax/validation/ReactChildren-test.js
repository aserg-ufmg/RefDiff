    Number.prototype['@@iterator'] = function () {
      throw new Error('number iterator called');
    };