import type { Metadata } from 'next';
import { buildLocalizedMetadata } from '@/lib/seo';
import { getDoctorServer } from '@/lib/server-api';
import DoctorDetailPageClient from './DoctorDetailPageClient';

type PageProps = {
  params: { locale: string; id: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const doctorId = Number(params.id);
  const doctor = Number.isNaN(doctorId) ? null : await getDoctorServer(doctorId, params.locale);

  const title = doctor
    ? `${doctor.name} ${doctor.specialty ? `- ${doctor.specialty}` : ''}`.trim()
    : 'Doctor profile';
  const description =
    doctor?.bio ||
    'Meet our experienced team of dentists and specialists providing preventive, cosmetic, and emergency dental care.';

  const keywords = doctor
    ? [doctor.name, doctor.specialty || 'dentist', 'dental specialist']
    : ['dentist profile', 'dental specialist'];

  return buildLocalizedMetadata({
    locale: params.locale,
    path: `/doctors/${params.id}`,
    title,
    description,
    keywords,
    image: doctor?.imageUrl || undefined,
  });
}

export default function DoctorDetailPage({ params }: PageProps) {
  const doctorId = Number(params.id);
  return <DoctorDetailPageClient doctorId={doctorId} />;
}
