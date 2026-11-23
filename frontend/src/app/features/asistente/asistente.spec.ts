import { partesDeRespuesta } from './asistente';

describe('partesDeRespuesta', () => {
  it('separa el texto resaltado con **negrita** (US21)', () => {
    expect(partesDeRespuesta('Hay **2 habitaciones** en **Barranco**.')).toEqual([
      { texto: 'Hay ', negrita: false },
      { texto: '2 habitaciones', negrita: true },
      { texto: ' en ', negrita: false },
      { texto: 'Barranco', negrita: true },
      { texto: '.', negrita: false },
    ]);
  });

  it('deja el texto sin marcas tal cual y no interpreta HTML', () => {
    expect(partesDeRespuesta('<b>hola</b>')).toEqual([{ texto: '<b>hola</b>', negrita: false }]);
  });
});
