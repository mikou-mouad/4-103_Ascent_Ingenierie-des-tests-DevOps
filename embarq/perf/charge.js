// Test de charge : l'ouverture de la saison, le 1er avril à 9h.
// 20 visiteurs simultanés pendant 30 secondes consultent les traversées et calculent un prix.
//
// docker run --rm -i --network lab grafana/k6:1.8.2 run -e BASE_URL=http://embarq-dev:8080 - < perf/charge.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 20 },   // montée en charge
    { duration: '30s', target: 20 },   // plateau
    { duration: '10s', target: 0 },    // descente
  ],
  // Les seuils font échouer le test (code de sortie non nul) s'ils ne sont pas tenus
  thresholds: {
    http_req_failed: ['rate<0.01'],     // moins de 1 % d'erreurs
    http_req_duration: ['p(95)<500'],   // 95 % des réponses en moins de 500 ms
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8090';

export default function () {
  const accueil = http.get(`${BASE_URL}/`);
  check(accueil, { 'accueil : 200': (r) => r.status === 200 });

  const liste = http.get(`${BASE_URL}/api/traversees`);
  check(liste, { 'traversées : 200': (r) => r.status === 200 });

  const tarif = http.get(`${BASE_URL}/api/tarifs?adultes=2&enfants=1`);
  check(tarif, { 'tarif famille : 90 €': (r) => r.json('montant') === 90 });

  sleep(1);
}
