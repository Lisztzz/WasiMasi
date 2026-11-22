import { DISTRITOS_LIMA, ZONAS_POPULARES, filtrarDistritos, normalizarZona, sinTildes } from './distritos';

/** US08 - Normalización de zonas para que la búsqueda ignore tildes y espacios. */
describe('distritos', () => {
  describe('sinTildes', () => {
    it('quita las tildes y conserva el resto del texto', () => {
      expect(sinTildes('Jesús María')).toBe('Jesus Maria');
      expect(sinTildes('San Isidro')).toBe('San Isidro');
    });
  });

  describe('normalizarZona', () => {
    it('quita tildes, recorta y compacta espacios', () => {
      expect(normalizarZona('  Jesús   María  ')).toBe('Jesus Maria');
      expect(normalizarZona('Santiago de Surco')).toBe('Santiago de Surco');
    });
  });

  describe('filtrarDistritos', () => {
    it('ignora mayúsculas y tildes al filtrar', () => {
      expect(filtrarDistritos('jesus maria')).toEqual(['Jesús María']);
      expect(filtrarDistritos('MIRAfLORES')).toEqual(['Miraflores', 'San Juan de Miraflores']);
    });

    it('devuelve varias coincidencias por substring', () => {
      expect(filtrarDistritos('san')).toEqual([
        'San Borja', 'San Isidro', 'San Juan de Lurigancho', 'San Juan de Miraflores',
        'San Martín de Porres', 'San Miguel', 'Santiago de Surco',
      ]);
    });

    it('devuelve todos los distritos si no hay texto y una lista vacía si no hay coincidencias', () => {
      expect(filtrarDistritos('   ')).toEqual([...DISTRITOS_LIMA]);
      expect(filtrarDistritos('No existe')).toEqual([]);
    });

    it('incluye las zonas populares entre los distritos de Lima', () => {
      for (const zona of ZONAS_POPULARES) {
        expect(DISTRITOS_LIMA).toContain(zona);
      }
    });
  });
});
